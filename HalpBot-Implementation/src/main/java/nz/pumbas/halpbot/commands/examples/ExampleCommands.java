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

package nz.pumbas.halpbot.commands.examples;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.permissions.Permissions;

@Service
public class ExampleCommands
{
//    @Override
//    public void onMessageReceived(MessageReceivedEvent event) {
//        Message message = event.getMessage();
//        String msg = message.getContentDisplay();
//        if (msg.startsWith("$kick")) {
//            if (message.isFromType(ChannelType.TEXT)) {
//                if (message.getMentionedUsers().isEmpty())
//                    event.getChannel().sendMessage("You must mention 1 or more Users to be kicked!").queue();
//                else {
//                    Member selfMember = event.getGuild().getSelfMember();
//                    if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
//                        event.getChannel().sendMessage("Sorry! I don't have permission to kick members in this Guild!").queue();
//                        return;
//                    }
//
//                    List<User> mentionedUsers = message.getMentionedUsers();
//                    Member member = event.getGuild().retrieveMember(mentionedUsers.get(0)).complete();
//                    if (!selfMember.canInteract(member)) {
//                        event.getChannel()
//                                .sendMessage("Cannot kick member: %s, they are higher in the heirarchy than I am"
//                                        .formatted(member.getEffectiveName()))
//                                .queue();
//                        return;
//                    }
//                    event.getGuild().kick(member)
//                            .queue((success) -> event.getChannel()
//                                    .sendMessage("Successfully kicked %s!".formatted(member.getEffectiveName()))
//                                    .queue());
//                }
//            }
//            else event.getChannel().sendMessage("This is a Guild-Only command!").queue();
//        }
//    }

//    // E.g: $pong
//    @Command(description = "Simple pong command")
//    public String pong(MessageReceivedEvent event) {
//        return event.getAuthor().getAsMention();
//    }
//
//    // E.g: $add 2 4.3
//    @Command(description = "Adds two numbers")
//    public double add(double num1, double num2) {
//        return num1 + num2;
//    }
//
//    // E.g: $pick yes no maybe or $choose yes no maybe
//    @Command(alias = { "pick", "choose" }, description = "Randomly chooses one of the items")
//    public String choose(@Implicit String[] choices) {
//        // Use of @Implicit means that it's not necessary to surround the choices with [...]
//        return choices[(int)(Math.random() * choices.length)];
//    }

//    // E.g: $whois @pumbas600
//    // By specifing the display duration, the returned result of this method is deleted after 2 minutes
//    @Command(description = "Display the information for a member", display = @Duration(value = 2, unit = ChronoUnit.MINUTES))
//    public MessageEmbed whoIs(Member member) {
//        User user = member.getUser();
//        List<Role> roles = member.getRoles();
//        String joinedRoles = roles.stream().map(Role::getAsMention).collect(Collectors.joining(" "));
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu"); // Formats the date as: 16 Jan 2022
//
//        return new EmbedBuilder()
//                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
//                .setThumbnail(user.getAvatarUrl())
//                .setColor(Color.ORANGE)
//                .setDescription(user.getAsMention())
//                .addField("Joined", member.getTimeJoined().format(formatter), true)
//                .addField("Registered", user.getTimeCreated().format(formatter), true)
//                .addField("Roles [%d]".formatted(roles.size()), joinedRoles, false)
//                .setFooter("ID: " + user.getId())
//                .build();
//    }

//
//
//    // The button adapter is a non-command parameter that is automatically passed into the parameters
//    @Command(description = "Tests passing a parameter to a dynamic button")
//    public void quiz(ButtonAdapter buttonAdapter, MessageReceivedEvent event) {
//        event.getChannel().sendMessage("Is 5 + 7 = 12?")
//                .setActionRow(
//                        // When the button is clicked, the @ButtonAction with the matching id is invoked
//                        buttonAdapter.register(Button.primary("halpbot.example.quiz", "Yes"), true),
//                        buttonAdapter.register(Button.primary("halpbot.example.quiz", "No"), false)
//                ).queue();
//    }
//
//    // The @Source User is extracted from the event, whereas the boolean (Which isn't a command parameter) is
//    // supplied by the parameters we registered it with.
//    @ButtonAction(id = "halpbot.example.quiz")
//    public String quizResult(@Source User user, boolean isCorrect) {
//        if (isCorrect)
//            return "Congratulations %s, you're correct!".formatted(user.getName());
//        return "Sorry %s, that wasn't the right answer :(".formatted(user.getName());
//    }

    // E.g: $kick @pumbas600 or $kick @pumbas600 some reason
    // Requires that the bot has the KICK_MEMBERS permission
    @Nullable
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
//
//    private final Map<Long, Map<Long, Integer>> bank = new ConcurrentHashMap<>();
//    private final Random random = new Random();
//
//    // Restrict it so that this member can only call the command once per hour per guild
//    @Cooldown(duration = @Duration(value = 1, unit = ChronoUnit.HOURS))
//    @Command(description = "Adds a random amount between $0 and $500 to the users account")
//    public String collect(@Source Guild guild, @Source Member member) {
//        long memberId = member.getIdLong();
//
//        int amount = this.random.nextInt(500);
//
//        // If there is no map for this guild, create a new one and return either the existing map or the newly created one
//        Map<Long, Integer> guildBank = this.bank.computeIfAbsent(
//                guild.getIdLong(), guildId -> new ConcurrentHashMap<>());
//
//        int newAmount = amount + guildBank.getOrDefault(memberId, 0);
//        guildBank.put(memberId, newAmount);
//
//        return "You collected %d. You now have %d in your account".formatted(amount, newAmount);
//    }
}
