package nz.pumbas;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Exceptions.ErrorMessageException;
import nz.pumbas.commands.Exceptions.UnimplementedFeatureException;
import nz.pumbas.customparameters.Shape;
import nz.pumbas.steamtables.ModelHelper;
import nz.pumbas.steamtables.SteamRow;
import nz.pumbas.steamtables.SteamTableManager;
import nz.pumbas.steamtables.annotations.Column;
import nz.pumbas.utilities.Singleton;
import nz.pumbas.customparameters.Vector2;

@CommandGroup(defaultPrefix = "$")
public class HalpBotCommands
{

    @Command(alias = "halp")
    public void onHalp(MessageReceivedEvent event)
    {
        event.getChannel().sendMessage("I will try my very best!").queue();
    }

    @Command(alias = "source")
    public void onSource(MessageReceivedEvent event)
    {
        event.getChannel().sendMessage("You can see the source code for me here: " +
            "https://github.com/pumbas600/HalpBot :kissing_heart:").queue();
    }

    @Command(alias = "suggestion")
    public void onSuggestion(MessageReceivedEvent event) {
        event.getChannel().sendMessage("You can note issues and suggestions for me here: " +
            "https://github.com/pumbas600/HalpBot/issues").queue();
    }

    @Command(alias = "components", command = "DOUBLE<n|N> <at> DOUBLE <from> (x|y)",
             description = "Splits a force into its x and y components")
    public void onComponents(MessageReceivedEvent event, double magnitude, double angle, String axis)
    {
        boolean fromX = "x".equals(axis) || "x-axis".equals(axis);
        Vector2 force = new Vector2(magnitude, angle, fromX);

        event.getChannel().sendMessage(force.toString()).queue();
    }

    @Command(alias = "shape", description = "Creates a shape object from a name and a number of sides")
    public void onShape(MessageReceivedEvent event, Shape shape, Shape shapeb)
    {
        event.getChannel()
            .sendMessage("You defined shape A as: " + shape.getName() + " with " + shape.getSides() + " sides!").queue();
        event.getChannel()
            .sendMessage("You defined shape B as: " + shapeb.getName() + " with " + shapeb.getSides() + " sides!").queue();
    }

    @Command(alias = "ping")
    public void onPing()
    {
        throw new UnimplementedFeatureException("This is still a work in progress, we'll try and get it finished as soon as possible!");
    }

    @Command(alias = "columns")
    public void onSteamColumn(MessageReceivedEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.orange);
        embedBuilder.setTitle("Steam Table Columns");

        StringBuilder columns = new StringBuilder();
        for (String column : SteamTableManager.Columns) {
            columns.append(column).append(" (").append(
                ModelHelper.getAnnotationFrom(SteamRow.class, column).units())
                .append(")\n");
        }
        embedBuilder.addField("Columns", columns.toString(), false);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    @Command(alias = "saturated", command = "<steam> WORD,? <where|when> WORD <=|is> DOUBLE")
    public void onSaturated(MessageReceivedEvent event, String selectColumn, String whereColumn, double value)
        throws SQLException
    {
        //TODO: WITHIN when there's not an exact value

        SteamTableManager steamTableManager = Singleton.getInstance(SteamTableManager.class);

        Optional<ResultSet> oResult = steamTableManager.selectRecord(
            selectColumn,whereColumn,value);
        if (oResult.isPresent()) {

            Column select =  ModelHelper.getAnnotationFrom(SteamRow.class, selectColumn);
            Column where = ModelHelper.getAnnotationFrom(SteamRow.class, whereColumn);

            ResultSet result = oResult.get();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.orange);
            embedBuilder.setTitle("Saturated Steam Look Up");
            embedBuilder.addField("Query",
                String.format("%s, where %s = %s %s", select.displayName(), where.displayName(), value, where.units()), false);

            StringBuilder resultBuilder = new StringBuilder();
            while (result.next()) {
                resultBuilder
                    .append(result.getString(selectColumn.toLowerCase()))
                    .append(" ")
                    .append(select.units());
            }
            String displayResult = resultBuilder.toString();
            if (displayResult.isEmpty()) {
                throw new ErrorMessageException(
                    "There doesn't seem to be any data for that query. Are you definitely looking for saturated steam?");
            }

            embedBuilder.addField("Result", displayResult, false);
            event.getChannel().sendMessage(embedBuilder.build()).queue();
        }
        else throw new ErrorMessageException("That doesn't seem to be a valid query.");
    }
}
