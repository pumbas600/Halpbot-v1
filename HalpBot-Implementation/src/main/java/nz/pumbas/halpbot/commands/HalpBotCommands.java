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

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
import nz.pumbas.halpbot.converters.DefaultConverters;
import nz.pumbas.halpbot.converters.TypeConverter;
import nz.pumbas.halpbot.customparameters.Shape;
import nz.pumbas.halpbot.customparameters.units.Prefix;
import nz.pumbas.halpbot.customparameters.units.Unit;

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

    @Command(description = "Randomly chooses one of the items")
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
            ctx -> DefaultConverters.DOUBLE_CONVERTER.apply(ctx)
                .map(value -> {
                    Exceptional<String> eUnit = DefaultConverters.STRING_CONVERTER.apply(ctx);
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
            DefaultConverters.ENUM_CONVERTER.apply(ctx)
                .map(prefix -> (Prefix) prefix)
                .orExceptional(() ->
                    DefaultConverters.CHARACTER_CONVERTER.apply(ctx)
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
}
