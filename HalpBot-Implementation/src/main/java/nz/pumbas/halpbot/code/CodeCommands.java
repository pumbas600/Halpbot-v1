package nz.pumbas.halpbot.code;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.data.mapping.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import nz.pumbas.halpbot.code.ExecutionResponse.Run;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;

@Service
public class CodeCommands extends ListenerAdapter
{
    @Inject private ObjectMapper objectMapper;
    @Inject private ApplicationContext applicationContext;

    private final List<String> supportedLanguages = new ArrayList<>();
    private final HttpClient client;
    private static final int OK = 200;

    public CodeCommands() {
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.requestRuntimes();
    }

    private void requestRuntimes() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://emkc.org/api/v2/piston/runtimes"))
                .GET()
                .build();

        this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == OK) {
                        this.objectMapper.read(response.body(), Language[].class)
                                .present(languages -> {
                                    for (Language language : languages) {
                                        this.supportedLanguages.addAll(language.allAliases());
                                    }
                                    this.applicationContext.log().info("Parsed request runtimes");
                                })
                                .caught(e -> this.applicationContext.log().error("Error parsing runtimes request response", e));
                    }
                });
    }

    @Nullable
    @Command(description = "Runs a piece of code", preserveWhitespace = true)
    public String run(MessageReceivedEvent event, String language, @Remaining String code) {
        if (language.startsWith("```") && code.endsWith("```")) {
            language = language.substring(3).toLowerCase(Locale.ROOT);
            code = code.substring(0, code.length() - 3);
        }

        if (language.isBlank())
            return "You must specify a language for the code";
        if (!this.supportedLanguages.contains(language))
            return "The language `%s` is not supported :(".formatted(language);

        if (code.isBlank())
            return "You must provide some code to run";

        Exceptional<String> json = CodeExecution.json(this.objectMapper, language, code);
        if (json.present()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://emkc.org/api/v2/piston/execute"))
                    .POST(BodyPublishers.ofString(json.get()))
                    .build();

            this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == OK) {
                            this.objectMapper.read(response.body(), ExecutionResponse.class)
                                    .present(executionResponse -> this.handleResponse(event, executionResponse))
                                    .caught(e -> this.applicationContext.log()
                                            .error("Caught the following error while parsing the execution response", e));
                        }
                    });

        }
        else return "There was an issue trying to parse the code into json";
        return null;
    }

    private void handleResponse(MessageReceivedEvent event, ExecutionResponse response) {
        Run run = response.run();
        String message;
        if (!run.stderr().isBlank())
            message = """
                    ```diff
                    - %s
                    ```
                    """.formatted(run.stderr());
        else if (!run.stdout().isBlank())
            message = """
                    ```
                    %s
                    ```
                    """.formatted(run.stdout());
        else message = "```No output```";

        if (message.length() > Message.MAX_CONTENT_LENGTH)
            message = message.substring(0, Message.MAX_CONTENT_LENGTH - 6) + "...```";

        event.getChannel().sendMessage(message).queue();

    }
}
