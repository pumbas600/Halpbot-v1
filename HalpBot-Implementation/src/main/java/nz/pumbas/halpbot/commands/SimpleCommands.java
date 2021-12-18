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

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class SimpleCommands
{
    @Inject private ApplicationContext applicationContext;

    private final List<String> comfortingMessages;
    private final List<String> insultJokes;

    public SimpleCommands() {
        this.comfortingMessages =
            HalpbotUtils.getAllLinesFromFile((HalpbotUtils.retrieveReader("ComfortingMessages.txt")).get());
        this.insultJokes =
            HalpbotUtils.getAllLinesFromFile((HalpbotUtils.retrieveReader("InsultJokes.txt")).get());
    }

    @Command(description = "Allows you to praise the bot")
    public String goodBot() {
        return HalpbotUtils.randomChoice(new String[]{ "Thank you!", "I try my best :)", ":heart:", "No worries >~<" });
    }

    @Command(alias = "is", command = "Integer [in] List",
             description = "Tests if the element is contained within the array")
    public String testing(int num, @Unrequired("[]") List<Integer> array) {
        return array.contains(num)
            ? "That number is in the array! :tada:"
            : "Sorry, it seems that number isn't in the array. :point_right: :point_left:";
    }

    @Command(description = "Sends a comforting message")
    public String comfort() {
        return HalpbotUtils.randomChoice(this.comfortingMessages);
    }

    @Command(description = "Sends a joking insult")
    public String insult() {
        return HalpbotUtils.randomChoice(this.insultJokes);
    }
}

