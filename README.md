# Halpbot ![JDK16](https://img.shields.io/badge/JDK-16-orange)

Halpbot is a comprehensive [JDA](https://github.com/DV8FromTheWorld/JDA) utility framework built using [Hartshorn](https://github.com/GuusLieben/Hartshorn) that provides a unique, annotation based approach to handling actions. It's key purpose is to alleviate as much unnecessary boilerplate code while simultaneously being both intuitive and customisable. To get started, check out the wiki [here](https://github.com/pumbas600/Halpbot/wiki).

## Why use Halpbot?

Halpbot is a feature rich library with support for message commands, buttons, decorators along with easy implementation of custom functionality or implementations. It's approach to handling actions is unlike any current JDA framework; in fact it more closely resembles the approach seen in [Discord.py](https://github.com/Rapptz/discord.py).

### Using Commands

Commands in Halpbot can simply be created by annotating a method with `@Command`. The method name will automatically be used as the alias (Although a different alias can be set within the annotation if desired) and the method parameters will act as command parameters. When invoked, it will automatically parse the parameters of the command and invoke the method. The returned result of the method is automatically displayed.

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.commands.annotations.Command;
```

</p>
</details>

```java
@Service
public class ExampleCommands
{
    @Command(description = "Simple pong command")
    public String pong(MessageReceivedEvent event) { // E.g: $pong
        return event.getAuthor().getAsMention();
    }
    
    @Command(description = "Adds two numbers")
    public double add(double num1, double num2) { // E.g: $add 2 4.3
        return num1 + num2;
    }
}
```

> **NOTE:** As the class is annotated with `@Service`, the commands will be automatically registered during startup. 

By default, Halpbot supports a vast range of parameter types as described [here](https://github.com/pumbas600/HalpBot/wiki/Command-Arguments), however, it's possible to easily create custom parameter converters.

### Using Buttons

Halpbot also provides an easy way of working with buttons, along with storing information within buttons by simply annotating the button callbacks with `@ButtonAction`. The method can take any source parameters.

Simple static buttons can simply be creating by setting the id of the `Button` to match the `@ButtonAction`:

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.utilities.Duration;
```

</p>
</details>


```java
@Service
public class ExampleCommands
{
    @Command(description = "Displays two test buttons")
    public void buttons(MessageReceivedEvent event) { // E.g: $buttons
        event.getChannel().sendMessage("Click on one of these buttons!")
                .setActionRow(
                        // When the button is clicked, the @ButtonAction with the matching id is invoked
                        Button.primary("halpbot.example.primary", "Primary button!"), 
                        Button.secondary("halpbot.example.secondary", "Secondary button!")
                ).queue();
    }

    @ButtonAction(id = "halpbot.example.primary")
    public String primary(ButtonClickEvent event) { // You can directly pass the event
        return "%s clicked the primary button!".formatted(event.getUser().getName());
    }
    
    // The display duration field specifies that the result should only be displayed for 20 seconds
    @ButtonAction(id = "halpbot.example.secondary", displayDuration = @Duration(20))
    public String secondary(@Source User user) { // Alternatively, you can retrieve fields from the event using @Source
        return "%s clicked the secondary button!".formatted(user.getName());
    }
}
```

It's also possible to store parameters that are to be passed to the button action when it's clicked, achieving 'dynamic' buttons:

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.buttons.ButtonAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
```

</p>
</details>

```java
@Service
public class ExampleCommands
{
    // The button adapter is a non-command parameter that is automatically passed into the parameters
    @Command(description = "Tests passing a parameter to a dynamic button")
    public void quiz(ButtonAdapter buttonAdapter, MessageReceivedEvent event) { // E.g: $quiz        
        event.getChannel().sendMessage("Is 5 + 7 = 12?")
                .setActionRow(
                        // When the button is clicked, the @ButtonAction with the matching id is invoked
                        buttonAdapter.register(Button.primary("halpbot.example.quiz", "Yes"), true),
                        buttonAdapter.register(Button.primary("halpbot.example.quiz", "No"), false)
                ).queue();
    }
    
    // The @Source User is extracted from the event, whereas the boolean (Which isn't a command parameter) is 
    // supplied by the parameters we registered it with.
    @ButtonAction(id = "halpbot.example.quiz")
    public String quizResult(@Source User user, boolean isCorrect) {
        if (isCorrect)
            return "Congratulations %s, you're correct!".formatted(user.getName());
        return "Sorry %s, that wasn't the right answer :(".formatted(user.getName());
    }
}
```

> **NOTE:** These parameters are only saved for as long as the bot is running. If the button is clicked after the bot has been restarted, it will not invoke the action method as these dynamic button's parameters are only stored in memory.

### Decorators

## Getting Started






HalpBot is an annotation based command framework for JDA. 
