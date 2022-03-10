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

package net.pumbas.halpbot.commands;

//import net.dv8tion.jda.api.EmbedBuilder;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//
//import java.awt.Color;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import Command;
//import ErrorMessageException;
//import net.pumbas.halpbot.commands.steamtables.SteamTable;
//import net.pumbas.halpbot.commands.steamtables.models.Model;
//import net.pumbas.halpbot.commands.steamtables.models.SaturatedSteamModel;
//import net.pumbas.halpbot.commands.steamtables.SteamTableManager;
//import net.pumbas.halpbot.utilities.Singleton;
//import net.pumbas.halpbot.utilities.enums.Flags;
//import net.pumbas.halpbot.utilities.maps.MapHelper;
//import net.pumbas.halpbot.utilities.maps.Row;

public class SteamLookupCommands
{
//    @Command(alias = "columns", description = "Lists all the available columns for saturated steam look ups")
//    public void onSteamColumn(MessageReceivedEvent event, SteamTable steamTable)
//    {
//        EmbedBuilder embedBuilder = new EmbedBuilder();
//        embedBuilder.setColor(Color.orange);
//        embedBuilder.setTitle("Steam Table Columns");
//
//        Map<String, List<String>> columnAliases = new HashMap<>();
//        Singleton.getInstance(SteamTableManager.class).getColumnMappings().forEach(columnModel -> {
//            if (Flags.hasFlag(columnModel.tables, steamTable)) {
//                if (!columnAliases.containsKey(columnModel.columnname))
//                    columnAliases.put(columnModel.columnname, new ArrayList<>());
//                columnAliases.get(columnModel.columnalias).add(columnModel.columnalias);
//            }
//        });
//        StringBuilder columns = new StringBuilder();
//        columnAliases.forEach((column, aliases) -> {
//            aliases.sort(Comparator.comparing(String::length));
//            columns.append(String.join(", ", aliases))
//                .append(" (")
//                .append(MapHelper.<String>getValue(steamTable.getModelType(), column, "units"))
//                .append(")\n");
//        });
//
//        embedBuilder.addField(String.format("%s Columns", steamTable.getDisplayName()), columns.toString(), false);
//        event.getChannel().sendMessage(embedBuilder.build()).queue();
//    }
//
//    private <T extends Model> void steamLookup(MessageReceivedEvent event, String selectColumn, String whereColumn,
//                                               double target, Class<T> steamModelType)
//    {
//        SteamTableManager steamTableManager = Singleton.getInstance(SteamTableManager.class);
//        SteamTable steamTable = SteamTable.of(steamModelType);
//
//        Optional<T> oSteamModel = steamTableManager.selectRecord(
//            steamModelType,
//            selectColumn,
//            whereColumn,
//            target);
//
//        selectColumn = steamTableManager.selectColumn(selectColumn, steamTable);
//        whereColumn = steamTableManager.selectColumn(whereColumn, steamTable);
//
//        if (oSteamModel.isPresent()) {
//            T steamModel = oSteamModel.get();
//
//            Row select = MapHelper.getFieldMap(steamModelType, selectColumn);
//            Row where = MapHelper.getFieldMap(steamModelType, whereColumn);
//
//            EmbedBuilder embedBuilder = new EmbedBuilder();
//            embedBuilder.setColor(Color.orange);
//            embedBuilder.setTitle(String.format("%s Look Up", steamTable.getDisplayName()));
//            embedBuilder.addField("Query",
//                String.format("Select %s when %s = %s %s", select.getValue("displayName"), where.getValue(
//                    "displayName"), target, where.getValue("units")), false);
//
//            String result = String.format("%s %s", steamModel.getDouble(selectColumn), select.getString("units"));
//            embedBuilder.addField("Result", result, false);
//
//            if (steamModel.getDouble(whereColumn) != target) {
//                embedBuilder.setFooter(String.format("I didn't have any information for when %s = %s %s,\nso I got " +
//                        "the next closest thing (%s = %s %s).",
//                    where.getValue("displayName"), target, where.getValue("units"),
//                    where.getValue("displayName"), steamModel.getDouble(whereColumn), where.getValue("units")));
//            }
//
//            event.getChannel().sendMessage(embedBuilder.build()).queue();
//        } else throw new ErrorMessageException("That doesn't seem to be a valid query.");
//    }
//
//    @Command(alias = "saturated", command = "<steam> WORD,? <where|when> WORD <=|is> DOUBLE")
//    public void onSaturated(MessageReceivedEvent event, String selectColumn, String whereColumn, double target)
//    {
//        this.steamLookup(event, selectColumn, whereColumn, target, SaturatedSteamModel.class);
//        SteamTableManager steamTableManager = Singleton.getInstance(SteamTableManager.class);
//        Optional<SaturatedSteamModel> oSaturatedModel = steamTableManager.selectRecord(SaturatedSteamModel.class,
//            selectColumn, whereColumn, target);
//
//        selectColumn = steamTableManager.selectColumn(selectColumn, SteamTable.SATURATED);
//        whereColumn = steamTableManager.selectColumn(whereColumn, SteamTable.SATURATED);
//
//        if (oSaturatedModel.isPresent()) {
//            SaturatedSteamModel saturatedModel = oSaturatedModel.get();
//
//            Row select = MapHelper.getFieldMap(SaturatedSteamModel.class, selectColumn);
//            Row where = MapHelper.getFieldMap(SaturatedSteamModel.class, whereColumn);
//
//            EmbedBuilder embedBuilder = new EmbedBuilder();
//            embedBuilder.setColor(Color.orange);
//            embedBuilder.setTitle("Saturated Steam Look Up");
//            embedBuilder.addField("Query",
//                String.format("Select %s when %s = %s %s", select.getValue("displayName"), where.getValue(
//                    "displayName"), target, where.getValue("units")), false);
//
//            String result = String.format("%s %s", saturatedModel.getDouble(selectColumn), select.getString("units"));
//            embedBuilder.addField("Result", result, false);
//
//            if (saturatedModel.getDouble(whereColumn) != target) {
//                embedBuilder.setFooter(String.format("I didn't have any information for when %s = %s %s,\nso I got " +
//                        "the next closest thing (%s = %s %s).",
//                    where.getValue("displayName"), target, where.getValue("units"),
//                    where.getValue("displayName"), saturatedModel.getDouble(whereColumn), where.getValue("units")));
//            }
//
//            event.getChannel().sendMessage(embedBuilder.build()).queue();
//        } else throw new ErrorMessageException("That doesn't seem to be a valid query.");
//    }
}
