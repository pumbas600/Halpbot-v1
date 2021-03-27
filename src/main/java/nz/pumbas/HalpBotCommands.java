package nz.pumbas;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Exceptions.ErrorMessageException;
import nz.pumbas.commands.Exceptions.UnimplementedFeatureException;
import nz.pumbas.customparameters.Shape;
import nz.pumbas.steamtables.SteamTable;
import nz.pumbas.steamtables.models.ModelHelper;
import nz.pumbas.steamtables.models.SaturatedSteamModel;
import nz.pumbas.steamtables.SteamTableManager;
import nz.pumbas.utilities.Singleton;
import nz.pumbas.customparameters.Vector2;
import nz.pumbas.utilities.Utilities;
import nz.pumbas.utilities.maps.Row;
import nz.pumbas.utilities.maps.MapHelper;

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


    @Command(alias = "columns", description = "Lists all the available columns for saturated steam look ups")
    public void onSteamColumn(MessageReceivedEvent event, SteamTable steamTable) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.orange);
        embedBuilder.setTitle("Steam Table Columns");

        List<String> columnNames = ModelHelper.getColumnNames(steamTable.getModelType());
        Map<String, List<String>> columnAliases = new HashMap<>();
        Singleton.getInstance(SteamTableManager.class).getColumnMappings().forEach((alias, column) -> {
            if (columnNames.contains(column)) {
                if (!columnAliases.containsKey(column))
                    columnAliases.put(column, new ArrayList<>());
                columnAliases.get(column).add(alias);
            }
        });
        StringBuilder columns = new StringBuilder();
        columnAliases.forEach((column, aliases) -> {
            aliases.sort(Comparator.comparing(String::length));
            columns.append(String.join(", ", aliases))
                .append(" (")
                .append(MapHelper.<String>getValue(steamTable.getModelType(), column, "units"))
                .append(")\n");
        });

        embedBuilder.addField(String.format("%s Columns", steamTable.getDisplayName()), columns.toString(), false);
        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }

    @Command(alias = "saturated", command = "<steam> WORD,? <where|when> WORD <=|is> DOUBLE")
    public void onSaturated(MessageReceivedEvent event, String selectColumn, String whereColumn, double target)
    {
        SteamTableManager steamTableManager = Singleton.getInstance(SteamTableManager.class);
        Optional<SaturatedSteamModel> oSaturatedModel = steamTableManager.selectRecord(SaturatedSteamModel.class,
            selectColumn, whereColumn, target);

        selectColumn = steamTableManager.selectColumn(selectColumn);
        whereColumn = steamTableManager.selectColumn(whereColumn);

        if (oSaturatedModel.isPresent()) {
            SaturatedSteamModel saturatedModel = oSaturatedModel.get();

            Row select =  MapHelper.getFieldMap(SaturatedSteamModel.class, selectColumn);
            Row where = MapHelper.getFieldMap(SaturatedSteamModel.class, whereColumn);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.orange);
            embedBuilder.setTitle("Saturated Steam Look Up");
            embedBuilder.addField("Query",
                String.format("Select %s when %s = %s %s", select.getValue("displayName"), where.getValue(
                    "displayName"), target, where.getValue("units")), false);

            String result = String.format("%s %s", saturatedModel.getDouble(selectColumn), select.getString("units"));
            embedBuilder.addField("Result", result, false);

            if (saturatedModel.getDouble(whereColumn) != target) {
                embedBuilder.setFooter(String.format("I didn't have any information for when %s = %s %s,\nso I got " +
                    "the next closest thing (%s = %s %s).",
                    where.getValue("displayName"), target, where.getValue("units"),
                    where.getValue("displayName"), saturatedModel.getDouble(whereColumn), where.getValue("units")));
            }

            event.getChannel().sendMessage(embedBuilder.build()).queue();
        }
        else throw new ErrorMessageException("That doesn't seem to be a valid query.");
    }
}
