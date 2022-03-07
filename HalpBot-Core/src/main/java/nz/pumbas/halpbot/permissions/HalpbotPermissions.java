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

package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import javax.inject.Inject;

import nz.pumbas.halpbot.HalpbotCore;

@Service
public class HalpbotPermissions
{
    @Inject
    private HalpbotCore halpbotCore;

    public static final String BOT_OWNER = "halpbot.bot.owner";
    public static final String GUILD_OWNER = "halpbot.guild.owner";

    @PermissionSupplier(GUILD_OWNER)
    public boolean isGuildOwner(Guild guild, Member member) {
        return guild.getOwnerIdLong() == member.getIdLong();
    }

    @PermissionSupplier(BOT_OWNER)
    public boolean isBotOwner(Guild guild, Member member) {
        return this.halpbotCore.ownerId() == member.getIdLong();
    }
}
