# Halpbot ![JDK16](https://img.shields.io/badge/JDK-16-orange)

Halpbot is a comprehensive [JDA](https://github.com/DV8FromTheWorld/JDA) utility framework built using [Hartshorn](https://github.com/GuusLieben/Hartshorn) that provides a unique, annotation based approach to handling actions. It's key purpose is to alleviate as much unnecessary boilerplate code while simultaneously being both intuitive and customisable. For more detailed information on the various features Halpbot has to offer, including a getting started tutorial for people new to Java, check out the [wiki](https://github.com/pumbas600/Halpbot/wiki).

# Why use Halpbot?

Halpbot is a feature rich library with support for message commands, triggers, buttons and decorators. Halpbot makes **virtually all** default implementations overridable if you desire. It's approach to handling actions is unlike any current JDA framework; in fact it more closely resembles the approach seen in [Discord.py](https://github.com/Rapptz/discord.py). Some examples of what Halpbot can do are shown below. Do note that these examples only cover a small fraction of the functionality Halpbot has to offer and I would highly recommend browsing the [wiki](https://github.com/pumbas600/Halpbot/wiki) to get a better appreciation for what's possible.

## 1.1 Getting started

There is currently not a version of Halpbot available on Maven as some work still needs to be done beforehand smoothing out some of the existing issues. If you desperately want to get started, you can manually build `Halpbot-Core` yourself. You'll also need to build the latest version of [Hartshorn](https://github.com/GuusLieben/Hartshorn) for `hartshorn-core`, `hartshorn-data` and `harshorn-configuration`.

## 1.2 Setting up your bot class

Halpbot is initialised from a `Bot` class. This class consists of 4 things of note:

1. Your bot class must be annotated with `@Service` and `@Activator`. You can also add additional activators to enable the various features Halpbot has to offer such as `@UseButtons` and `@UseCommands`.
2. The bot class must implement the `Bot` interface.
3. Within the main method, you can call `HalpbotBuilder#build` with the bot class and the main args. This constructs your bot and automatically begins registering the actions defined by your bot.
4. The initialise method allows you create the JDABuilder and configure it as you would like, before returning it to be used by Halpbot to register additional event listeners.

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.buttons.UseButtons;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.common.Bot;
```

</p>
</details>

```java
// 1. You must annotate your bot with @Service and @Activator. Additional activators can also be added to this class,
// such as @UseButtons and @UseCommands. 
@Service
@Activator
@UseButtons
@UseCommands
public class ExampleBot implements Bot // 2. Your bot class must implement the Bot interface
{
    public static void main(String[] args) throws ApplicationException {
        HalpbotBuilder.build(ExampleBot.class, args); // 3. Starts the bot using this class
    }

    // 4. The initialise method allows you to configure the JDABuilder as you like
    @Override
    public JDABuilder initialise(String[] args) {
        String token = args[0]; // The token is the first argument
        return JDABuilder.createDefault(token)
                .setActivity(Activity.of(ActivityType.LISTENING, "to how cool Halpbot is!"));
    }
}
```

If you want, you can make your bot class extend `ListenerAdapter` to utilise JDA's built in events, for example:

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import javax.inject.Inject;

import nz.pumbas.halpbot.buttons.UseButtons;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.common.Bot;
```

</p>
</details>

```java
@Service
@Activator
@UseButtons
@UseCommands
public class ExampleBot extends ListenerAdapter implements Bot
{
    @Inject private ApplicationContext applicationContext;
    
    public static void main(String[] args) throws ApplicationException {
        HalpbotBuilder.build(ExampleBot.class, args);
    }
    
    @Override
    public void onReady(ReadyEvent event) {
        // When the bot starts up, log the number of guilds it's in
        this.applicationContext.log().info("Bot running in %d guilds".formatted(event.getGuildTotalCount()));
    }

    @Override
    public JDABuilder initialise(String[] args) {
        String token = args[0]; // The token is the first argument
        return JDABuilder.createDefault(token)
                .addEventListeners(this) // Add this class as an event listener
                .setActivity(Activity.of(ActivityType.LISTENING, "to how cool Halpbot is!"));
    }
}
```

## 1.3 Bot config



## 2.1 Commands

In Halpbot, commands are simply created by annotating a method with `@Command`. You can enable commands in Halpbot by adding `@UseCommands` to your bot class. The method name will automatically be used as the alias (Although additional aliases can be set within the annotation if desired using the `alias` field). Command methods **must be public**; A warning will be logged during startup if you try and register non-public command methods. In Command methods, the parameters will act as command parameters.

There are two types of command parameters:
1. [Source Parameters]() - These are either injected services or information extracted from the event.
2. [Command Parameters]() - These are non-source parameters which are expected to be specified when invoking the command. These are automatically parsed from the command.

If a parameter was expected but wasn't present or didn't match the expected format, then the command will not be invoked and a temporary message will be sent to the user with the error. By default, Halpbot supports a vast range of parameter types as described [here](https://github.com/pumbas600/HalpBot/wiki/Command-Arguments), however, it's also possible to create parameter converters to add support for custom types or annotations. Finally, the returned result of the method is then automatically displayed to the user. 

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
    // E.g: $pong
    @Command(description = "Simple pong command")
    public String pong(MessageReceivedEvent event) {
        return event.getAuthor().getAsMention();
    }

    // E.g: $add 2 4.3
    @Command(description = "Adds two numbers")
    public double add(double num1, double num2) {
        return num1 + num2;
    }

    // E.g: $pick yes no maybe or $choose yes no maybe
    @Command(alias = { "pick", "choose" }, description = "Randomly chooses one of the items")
    public String choose(@Implicit String[] choices) {
        // Use of @Implicit means that it's not necessary to surround the choices with [...]
        return choices[(int)(Math.random() * choices.length)];
    }
}
```

> **NOTE:** Command aliases are **not** case sensitive.

Alternatively, if you want to make the returned result of the command be deleted after a period of time, you can specify a display duration. After which, the message will automatically be deleted. In the below example, the `MessageEmbed` is deleted after 2 minutes.

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.utilities.Duration;
```

</p>
</details>

```java
@Service
public class ExampleCommands
{
    // E.g: $whois @pumbas600
    // By specifing the display duration, the returned result of this method is deleted after 2 minutes
    @Command(description = "Display the information for a member", display = @Duration(value = 2, unit = ChronoUnit.MINUTES))
    public MessageEmbed whoIs(Member member) {
        User user = member.getUser();
        List<Role> roles = member.getRoles();
        String joinedRoles = roles.stream().map(Role::getAsMention).collect(Collectors.joining(" "));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu"); // Formats the date as: 16 Jan 2022

        return new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                .setThumbnail(user.getAvatarUrl())
                .setColor(Color.ORANGE)
                .setDescription(user.getAsMention())
                .addField("Joined", member.getTimeJoined().format(formatter), true)
                .addField("Registered", user.getTimeCreated().format(formatter), true)
                .addField("Roles [%d]".formatted(roles.size()), joinedRoles, false)
                .setFooter("ID: " + user.getId())
                .build();
    }
}
```

> **NOTE:** As the class is annotated with `@Service`, the commands will be automatically registered during startup. 

## 2.2 Triggers

Sometimes you want to respond to messages that contains certain words or phrases. This can be achieved in Halpbot using the `@Trigger` annotation. To enable triggers, add the `@UseTriggers` annotation to your bot class. The triggers for a method are **not** case sensitive. Like with commands, triggers can take in parameters, however, they only support [source parameters](). You can also return any object from the trigger method and it will automatically be sent to the channel. 

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.awt.Color;

import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.utilities.Require;
import nz.pumbas.halpbot.triggers.Trigger;
import nz.pumbas.halpbot.triggers.TriggerStrategy;
import nz.pumbas.halpbot.utilities.Duration;
```

</p>
</details>

```java
@Service
public class ExampleTriggers
{
    // Respond to any messages that start with any of the following triggers
    @Trigger({"hi", "hello", "hey"})
    public String sayHello(@Source Member member) {
        return "Hey %s!".formatted(member.getEffectiveName());
    }

    // Repond to any messages that contain 'my id' or 'discord id' anywhere with their id for 30 seconds
    @Trigger(value = { "my id", "discord id" }, strategy = TriggerStrategy.ANYWHERE, display = @Duration(30))
    public MessageEmbed usersId(@Source User user) {
        return new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                .setColor(Color.CYAN)
                .setDescription("Your id is: " + user.getId())
                .build();
    }

    // Respond to any messages that contains all the triggers. Require.ALL automatically sets the strategy to ANYWHERE
    @Trigger(value = {"found", "bug"}, require = Require.ALL)
    public String foundBug() {
        return this.howToReportIssue();
    }

    @Trigger(value = {"how", "report", "issue"}, require = Require.ALL)
    public String howToReportIssue() {
        return "You can report any issues you find here: https://github.com/pumbas600/Halpbot/issues/new/choose";
    }
}
```

> **NOTE:** By default, the `TriggerStrategy` is `START`, which checks if the message starts with the specified triggers.

## 2.3 Buttons

Halpbot also provides an easy way of working with buttons by simply annotating the button callbacks with `@ButtonAction`. To enable buttons, annotate your bot class with `@UseButtons`. These button action methods can only take [source parameters]() as arguments; These parameters can be in any order. To reference a button callback, all that you need to do is set the id of the `Button` to match the `@ButtonAction`. It will then automatically invoke the matching button action when the button is pressed.

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
public class ExampleButtons
{
    @Command(description = "Displays two test buttons")
    public void buttons(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Click on one of these buttons!")
                .setActionRow(
                        // When the button is clicked, the @ButtonAction with the matching id is invoked
                        Button.primary("halpbot:example:primary", "Primary button!"),
                        Button.secondary("halpbot:example:secondary", "Secondary button!")
                ).queue();
    }

    @ButtonAction(id = "halpbot:example:primary")
    public String primary(ButtonClickEvent event) { // You can directly pass the event
        return "%s clicked the primary button!".formatted(event.getUser().getName());
    }

    // The display field specifies that the result should only be displayed for 20 seconds before being deleted
    @ButtonAction(id = "halpbot:example:secondary", display = @Duration(20))
    public String secondary(@Source User user) { // Alternatively, you can retrieve fields from the event using @Source
        return "%s clicked the secondary button!".formatted(user.getName());
    }
}
```

> **NOTE:** Like with commands, as the class is annotated with `@Service`, the button actions are automatically registered during startup.

Halpbot also has support for storing objects with button instances which can be passed to the button action when invoked. This can be ideal in certain situations where you want to update an object based on the click of a specific button. For more information on these dynamic buttons, refer to the documentation [here]().

## 2.4 Decorators

Halpbot comes with three built-in [decorators](https://github.com/pumbas600/Halpbot/wiki/Decorators), however, the two main ones are the [cooldown](https://github.com/pumbas600/Halpbot/wiki/Decorators#cooldown) and [permissions](https://github.com/pumbas600/Halpbot/wiki/Decorators#permissions) decorators. Decorators are annotations that can be added to actions (`@Command`, `@Trigger`, or `@ButtonAction`) that modify how the method is called, or if it's even called at all. If you want to create your own decorators or override the default decorators, this can easily be done as described in the wiki [here](https://github.com/pumbas600/Halpbot/wiki/Custom-Decorators).

<details>
<summary>Show Imports</summary>
<p>

```java
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.actions.cooldowns.Cooldown;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.utilities.Duration
```

</p>
</details>

```java
@Service
public class ExampleCommands
{
    private final Map<Long, Map<Long, Integer>> bank = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Restrict it so that this member can only call the command once per hour per guild
    @Cooldown(duration = @Duration(value = 1, unit = ChronoUnit.HOURS))
    @Command(description = "Adds a random amount between $0 and $500 to the users account")
    public String collect(@Source Guild guild, @Source Member member) {
        long memberId = member.getIdLong();

        int amount = this.random.nextInt(500);

        // If there is no map for this guild, create a new one and return either the existing map or the newly created one
        Map<Long, Integer> guildBank = this.bank.computeIfAbsent(
                guild.getIdLong(), guildId -> new ConcurrentHashMap<>());

        int newAmount = amount + guildBank.getOrDefault(memberId, 0);
        guildBank.put(memberId, newAmount);

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
    // E.g: $kick @pumbas600 or $kick @pumbas600 some reason
    // Requires that the bot has the KICK_MEMBERS permission
    @Permissions(self = Permission.KICK_MEMBERS)
    @Command(description = "Kicks a member from the guild")
    public String kick(MessageReceivedEvent event, Member member,
                       @Remaining @Unrequired("No reason specified") String reason)
    {
        if (!event.getGuild().getSelfMember().canInteract(member))
            return "Cannot kick member: %s, they are higher in the heirarchy than I am".formatted(member.getEffectiveName());

        event.getGuild().kick(member, reason)
                .queue((success) -> event.getChannel()
                        .sendMessage("Successfully kicked %s!".formatted(member.getEffectiveName()))
                        .queue());
        return null; // Don't respond via halpbot as we're queueing a response normally
    }
}
```

> **NOTE:** It's also possible to add permissions that the user must have, along with creating your own custom [permission suppliers](https://github.com/pumbas600/Halpbot/wiki/Permissions#permission-suppliers).
