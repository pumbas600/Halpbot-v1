/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.actions.annotations.Action;
import nz.pumbas.halpbot.actions.annotations.ButtonAction;
import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.adapters.ButtonAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Description;
import nz.pumbas.halpbot.commands.annotations.Implicit;
import nz.pumbas.halpbot.commands.annotations.SlashCommand;
import nz.pumbas.halpbot.converters.Converters;
import nz.pumbas.halpbot.converters.TypeConverter;
import nz.pumbas.halpbot.customparameters.Shape;
import nz.pumbas.halpbot.customparameters.units.Prefix;
import nz.pumbas.halpbot.customparameters.units.Unit;
import nz.pumbas.halpbot.objects.Exceptional;

public class HalpBotCommands
{

    @Command(alias = "source")
    public String source() {
        return "You can see the source code for me here: https://github.com/pumbas600/HalpBot :kissing_heart:";
    }

    @Command(alias = "invite", description = "Retrieves the invite for this discord bot")
    public String invite() {
        return "https://canary.discord.com/api/oauth2/authorize?client_id=819840092327772170&permissions=2147544128&scope=bot%20applications.commands";
    }

    @Command(alias = "suggestion")
    public String suggestion() {
        return "You can note issues and suggestions for me here: https://github.com/pumbas600/HalpBot/issues";
    }

    @Command(alias = "choose", description = "Randomly chooses one of the items")
    public String choose(@Implicit String[] choices) {
        // Use of @Implicit means that it's not necessary to surround the choices with [...]
        return choices[(int)(Math.random() * choices.length)];
    }

    @Command(alias = "centroid", description = "Finds the centroid defined by the specified shapes")
    public String centroid(@Implicit Shape[] shapes)
    {
        double sumAx = 0;
        double sumAy = 0;
        double totalA = 0;

        for (Shape shape : shapes) {
            sumAx += shape.getArea() * shape.getxPos();
            sumAy += shape.getArea() * shape.getyPos();
            totalA += shape.getArea();
        }

        return String.format("x: %.2f, y: %.2f", sumAx / totalA, sumAy / totalA);
    }

    public static final TypeConverter<Unit> UNIT_CONVERTER = TypeConverter.builder(Unit.class)
        .convert(
            ctx -> Converters.DOUBLE_CONVERTER.getMapper()
                .apply(ctx)
                .map(value -> {
                    Exceptional<String> eUnit = Converters.STRING_CONVERTER.getMapper().apply(ctx);
                    if (eUnit.caught()) eUnit.rethrow();
                    String unit = eUnit.get();
                    if (1 < unit.length() && Prefix.isPrefix(unit.charAt(0)))
                        return new Unit(value, Prefix.getPrefix(unit.charAt(0)), unit.substring(1));
                    else
                        return new Unit(value, Prefix.DEFAULT, unit);
                }))
        .register();

    public static final TypeConverter<Prefix> PREFIX_CONVERTER = TypeConverter.builder(Prefix.class)
        .convert(ctx ->
            Converters.ENUM_CONVERTER.getMapper().apply(ctx)
                .map(prefix -> (Prefix) prefix)
                .orExceptional(() ->
                    Converters.CHARACTER_CONVERTER.getMapper().apply(ctx)
                        .map(prefix -> {
                            if (Prefix.isPrefix(prefix))
                                return Prefix.getPrefix(prefix);
                            throw new IllegalArgumentException("That is not a valid prefix");
                        })
                ))
        .register();

    @Command(alias = "convert", description = "Converts the number to the specified prefix")
    public Unit convert(Unit unit, Prefix toPrefix) {
        return unit.to(toPrefix);
    }

    @SlashCommand
    @Command(description = "Sums two numbers")
    public void sum(ButtonAdapter buttonAdapter,
                    Interaction interaction,
                    @Description("The first number")  int a,
                    @Description("The second number") int b)
    {
        int sum = a + b;
        String message = String.format("The sum is %d :tada:", sum);
        interaction.reply(message)
            .addActionRow(
                buttonAdapter.register(Button.primary("add", "+1"), sum, 1),
                buttonAdapter.register(Button.danger("add", "-1"), sum, -1))
            .queue();
    }

    @ButtonAction
    @Cooldown
    @Action(listeningDuration = 10, displayDuration=30)
    public String add(ButtonClickEvent event, int sum, int amount) {
        return String.format("%d + %d is %d!", sum, amount, sum + amount);
    }

    @SlashCommand
    @Command(description = "Tests static button callbacks")
    public void test(Interaction interaction) {
        interaction.reply("This is a button test :)")
            .addActionRow(
                Button.primary("primaryButton", "Hi"),
                Button.secondary("secondaryButton", ":fire:"))
            .queue();
    }


    @ButtonAction
    @Action(displayDuration = 10)
    public String primaryButton(ButtonClickEvent event) {
        return "Hey there! o/";
    }

    @ButtonAction(isEphemeral = true)
    @Action
    public String secondaryButton(ButtonClickEvent event) {
        return "So you choose fire... :eyes:";
    }
}
