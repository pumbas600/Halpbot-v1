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

package nz.pumbas.halpbot.pagination;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.InteractionEvent;

public interface Pagination<T>
{
    String getTitle();

    /**
     * @return The current page, starting at 0
     */
    int currentPage();

    /**
     * @return The number of items that should be stored per page
     */
    int itemsPerPage();

    /**
     * @return The total number of items that need to be displayed
     */
    int totalItems();

    /**
     * @return The total number of pages
     */
    default int totalPages() {
        return this.totalItems() / this.itemsPerPage() + 1;
    }

    /**
     * @return If this pagination has another page after the current one
     */
    default boolean hasNextPage() {
        return this.currentPage() < this.totalPages() - 1;
    }

    /**
     * @return If this pagination has a page before the current one
     */
    default boolean hasPreviousPage() {
        return 0 != this.currentPage();
    }

    /**
     * Retrieves a collection of the items on the specified page
     *
     * @param page
     *        The page to retrieve the items for
     *
     * @return A collection of the items on the specified page
     */
    Collection<T> getPageItems(int page);

    /**
     * @return The id of the message corresponding to this pagination.
     * @throws IllegalAccessError
     *         If you call this method before the embed has been sent
     */
    long getMessageId() throws IllegalAccessError;

    /**
     * Displays this pagination
     *
     * @param event
     *        The event that triggered the sending of this pagination
     */
    default void display(HalpbotEvent event) {
        if (event instanceof InteractionEvent) {
            event.event(Interaction.class)
                .replyEmbeds(this.buildCurrentPage())
                .queue();
        }
    }

    default MessageEmbed buildCurrentPage() {
        EmbedBuilder builder = new EmbedBuilder();
        this.buildTitle(builder);
        this.buildPageContent(builder, this.getPageItems(this.currentPage()));
        this.buildFooter(builder);
        return builder.build();
    }

    default void buildTitle(EmbedBuilder builder) {
        builder.setTitle(this.getTitle());
    }

    default void buildPageContent(EmbedBuilder builder, Collection<T> items) {
        for (T item : items) {
            String displayItem = this.getDisplayConverter().apply(item);
            this.getItemAdder().accept(builder, displayItem);
        }
    }

    default void buildFooter(EmbedBuilder builder) {
        builder.setFooter(String.format("Page %d/%d", this.currentPage() + 1, this.totalPages()));
    }

    default BiConsumer<EmbedBuilder, String> getItemAdder() {
        return (builder, item) -> builder.appendDescription(item).appendDescription("\n");
    }

    default Function<T, String> getDisplayConverter() {
        return Object::toString;
    }
}
