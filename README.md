# Halpbot ![JDK16](https://img.shields.io/badge/JDK-16-orange)

Halpbot is a comprehensive [JDA](https://github.com/DV8FromTheWorld/JDA) utility framework built using [Hartshorn](https://github.com/GuusLieben/Hartshorn) that provides a unique, annotation based approach to handling actions. It's key purpose is to alleviate as much unnecessary boilerplate code while simultaneously being both intuitive and customisable. To get started, check out the wiki [here](https://github.com/pumbas600/Halpbot/wiki).

## Why use Halpbot?

Halpbot is a feature rich library with support for message commands, buttons, decorators along with easy implementation of custom functionality or implementations. It's approach to handling actions is unlike any current JDA framework; in fact it more closely resembles the approach seen in [Discord.py](https://github.com/Rapptz/discord.py). Some examples of what Halpbot can do are shown below. Do note that these examples only cover a small fraction of the functionality Halpbot has to offer and I would highly recommend taking a browse through the [wiki](https://github.com/pumbas600/Halpbot/wiki) to get a better appreciation for what's possible.

### Using Commands

Commands in Halpbot can simply be created by annotating a method with `@Command`. The method name will automatically be used as the alias (Although a different alias can be set within the annotation if desired) and the method parameters will act as command parameters. When invoked, it will automatically parse the parameters of the command and invoke the method. The returned result of the method is automatically displayed. 

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
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

    @Command(description = "Randomly chooses one of the items")
    public String choose(@Implicit String[] choices) { // E.g: $choose yes no maybe
        // Use of @Implicit means that it's not necessary to surround the choices with [...]
        return choices[(int)(Math.random() * choices.length)];
    }
}
```

> **NOTE:** As the class is annotated with `@Service`, the commands will be automatically registered during startup. 

By default, Halpbot supports a vast range of parameter types as described [here](https://github.com/pumbas600/HalpBot/wiki/Command-Arguments), however, it's possible to easily create custom parameter converters to add support for custom types or annotations.

### Using Buttons

Halpbot also provides an easy way of working with buttons by simply annotating the button callbacks with `@ButtonAction`. These button action methods can take any [source parameters](https://github.com/pumbas600/Halpbot/wiki/Command-Arguments#source-converters) as arguments (A source parameter is anything that can be extracted from the event or injected) and in any order. To reference a button callback, all that you need to do is set the id of the `Button` to match the `@ButtonAction`:

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

> **NOTE:** Like with commands, as the class is annotated with `@Service`, the button actions are automatically registered during startup.

### Decorators

Halpbot comes with three built-in [decorators](https://github.com/pumbas600/Halpbot/wiki/Decorators), however, the two main ones are the [cooldown](https://github.com/pumbas600/Halpbot/wiki/Decorators#cooldown) and [permissions](https://github.com/pumbas600/Halpbot/wiki/Decorators#permissions) decorators. Decorators are annotations that can be added to actions (`@Command` or `@ButtonAction`) that modify how the method is called, or if it's even called at all. 

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
    
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import nz.pumbas.halpbot.actions.cooldowns.Cooldown;
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
    private final Map<Long, Integer> bank = new HashMap<>();
    private final Random random = new Random();
    
    // Restrict it so that this user can only call the command once per hour
    @Cooldown(duration = @Duration(value = 1, unit = ChronoUnit.HOURS))
    @Command(description = "Adds a random amount between $0 and $500 to the users account")
    public String collect(@Source User user) {
        long userId = user.getIdLong();

        int amount = this.random.nextInt(500);
        this.bank.putIfAbsent(userId, 0);
        int newAmount = amount + this.bank.get(userId);
        this.bank.put(userId, newAmount);

        return "You collected %d. You now have %d in your account".formatted(amount, newAmount);
    }
}
```

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.permissions.Permissions;
```

</p>
</details>

```java
@Service
public class ExampleCommands
{
    // Requires the bot to have the KICK_MEMBERS permission and that the bot can interact with the member.
    @Permissions(self = Permission.KICK_MEMBERS, canInteract = true)
    @Command(description = "Kicks a member from the guild")
    public void kick(MessageReceivedEvent event, Member member, // E.g: $kick @pumbas600 or $kick @pumbas600 for being too cool
                     @Remaining @Unrequired("No reason specified") String reason)
    {
        event.getGuild().kick(member, reason)
                .queue((v) -> event.getChannel()
                        .sendMessage("Successfully kicked %s!".formatted(member.getEffectiveName())));
    }
}
```

> **NOTE:** It's also possible to add permissions that the user must have, along with creating your own custom [permission suppliers](https://github.com/pumbas600/Halpbot/wiki/Permissions#permission-suppliers).

## Getting Started






HalpBot is an annotation based command framework for JDA. 
