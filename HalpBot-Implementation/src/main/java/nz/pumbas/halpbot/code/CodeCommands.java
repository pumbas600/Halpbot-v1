package nz.pumbas.halpbot.code;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.dockbox.hartshorn.core.ArrayListMultiMap;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.data.mapping.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import nz.pumbas.halpbot.RequestUtil;
import nz.pumbas.halpbot.code.ExecutionResponse.Run;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.decorators.log.Log;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Log
@Service
public class CodeCommands extends ListenerAdapter
{
    @Inject private ObjectMapper objectMapper;
    @Inject private ApplicationContext applicationContext;
    @Inject private RequestUtil requestUtil;

    @Nullable
    private MessageEmbed supportedLanguagesEmbed;
    private final Map<String, String> aliases = new HashMap<>();
    private final List<String> supportedLanguages = new ArrayList<>();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.requestRuntimes();
    }

    private void requestRuntimes() {
        this.requestUtil.getAsync("https://emkc.org/api/v2/piston/runtimes", Language[].class)
                .thenAccept(languages -> {
                    int i = 0;
                    int page = 1;
                    int oneThird = (int) Math.ceil(languages.length / 3D);
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle("Supported Languages")
                            .setColor(Color.ORANGE)
                            .setFooter("Use the `aliases <language>` command to find the supported aliases");

                    StringBuilder builder = new StringBuilder();
                    List<Language> sortedLanguages = Arrays.stream(languages)
                            .sorted(Comparator.comparing(Language::language))
                            .toList();

                    for (Language language : sortedLanguages) {
                        builder.append("- ")
                                .append(language.language())
                                .append("\n");

                        i++;
                        if (i >= oneThird) {
                            i = 0;
                            embedBuilder.addField("Page " + page, builder.toString(), true);
                            builder = new StringBuilder();
                            page++;
                        }

                        StringBuilder aliasBuilder = new StringBuilder();
                        for (String alias : language.aliases()) {
                            aliasBuilder.append("- ").append(alias).append("\n");
                        }
                        this.supportedLanguages.addAll(language.allAliases());
                        this.aliases.put(language.language(), aliasBuilder.toString());
                    }

                    embedBuilder.addField("Page " + page, builder.toString(), true);
                    this.supportedLanguagesEmbed = embedBuilder.build();
                    this.applicationContext.log().info("Parsed request runtimes");
                })
                .exceptionally(e -> {
                    this.applicationContext.log().error("Error parsing runtimes request response", e);
                    return null;
                });
    }

    @Command(alias = { "langs", "languages" }, description = "Lists the supported languages in the `run` command")
    public Object languages() {
        return Objects.requireNonNullElse(
                this.supportedLanguagesEmbed,
                "The supported languages are still being fetched...");
    }

    @Command(description = "Lists the aliases for the languages that can be used in `run` command")
    public Object aliases(String language) {
        language = language.toLowerCase(Locale.ROOT);
        if (!this.aliases.containsKey(language)) {
            return "The language `%s` isn't supported. Use the `languages` command to see a list of the supported languages"
                    .formatted(language);
        }

        String aliases = this.aliases.get(language);
        if (aliases.isBlank())
            aliases = "%s has no aliases".formatted(language);

        return new EmbedBuilder()
                .setTitle("%s's Aliases".formatted(HalpbotUtils.capitalise(language)))
                .setColor(Color.ORANGE)
                .setDescription(aliases)
                .build();
    }

    @Nullable
    @Command(description = "Runs a piece of code using the piston API", preserveWhitespace = true)
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
            this.requestUtil.postAsync("https://emkc.org/api/v2/piston/execute", json.get(), ExecutionResponse.class)
                    .thenAccept(executionResponse -> this.handleResponse(event, executionResponse))
                    .exceptionally(e -> {
                        this.applicationContext.log().error("Caught the following error while parsing the execution response", e);
                        return null;
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
                    ```fix
                    %s
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
