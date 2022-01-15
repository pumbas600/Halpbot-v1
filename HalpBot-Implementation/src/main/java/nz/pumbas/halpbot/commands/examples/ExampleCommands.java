package nz.pumbas.halpbot.commands.examples;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

@Service
public class ExampleCommands
{
//    private final Map<Long, Integer> bank = new HashMap<>();
//    private final Random random = new Random();

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
//    @Command(description = "Displays two test buttons")
//    public void buttons(MessageReceivedEvent event) {
//        event.getChannel().sendMessage("Click on one of these buttons!")
//                .setActionRow(
//                        // When the button is clicked, the @ButtonAction with the matching id is invoked
//                        Button.primary("halpbot.example.primary", "Primary button!"),
//                        Button.secondary("halpbot.example.secondary", "Secondary button!")
//                ).queue();
//    }
//
//    @ButtonAction(id = "halpbot.example.primary")
//    public String primary(ButtonClickEvent event) { // You can directly pass the event
//        return "%s clicked the primary button!".formatted(event.getUser().getName());
//    }
//
//    // The display duration field specifies that the result should only be displayed for 20 seconds
//    @ButtonAction(id = "halpbot.example.secondary", displayDuration = @Duration(20))
//    public String secondary(@Source User user) { // Alternatively, you can retrieve fields from the event using @Source
//        return "%s clicked the secondary button!".formatted(user.getName());
//    }
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
    // Requires that the bot has the KICK_MEMBERS permission.
//    @Nullable
//    @Permissions(self = Permission.KICK_MEMBERS)
//    @Command(description = "Kicks a member from the guild")
//    public String kick(MessageReceivedEvent event, Member member,
//                       @Remaining @Unrequired("No reason specified") String reason)
//    {
//        if (!event.getGuild().getSelfMember().canInteract(member))
//            return "Cannot kick member: %s, they are higher in the heirarchy than I am".formatted(member.getEffectiveName());
//
//        event.getGuild().kick(member, reason)
//                .queue((success) -> event.getChannel()
//                        .sendMessage("Successfully kicked %s!".formatted(member.getEffectiveName()))
//                        .queue());
//        return null; // Don't respond via halpbot as we're queueing a response normally
//    }
//
//    // Restrict it so that this user can only call the command once per hour
//    @Cooldown(duration = @Duration(value = 1, unit = ChronoUnit.HOURS))
//    @Command(description = "Adds a random amount between $0 and $500 to the users account")
//    public String collect(@Source User user) {
//        long userId = user.getIdLong();
//
//        int amount = this.random.nextInt(500);
//        this.bank.putIfAbsent(userId, 0);
//        int newAmount = amount + this.bank.get(userId);
//        this.bank.put(userId, newAmount);
//
//        return "You collected %d. You now have %d in your account".formatted(amount, newAmount);
//    }
}
