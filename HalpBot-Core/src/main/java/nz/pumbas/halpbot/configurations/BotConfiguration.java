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

package nz.pumbas.halpbot.configurations;


import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.data.annotations.Configuration;
import org.dockbox.hartshorn.data.annotations.Value;

import lombok.Getter;

@Getter
@Service
@Configuration(source = "classpath:bot-config.properties")
public class BotConfiguration
{
    @Value("ownerId")
    private long ownerId = -1;

    @Value("token")
    private String token = "";

    @Value("defaultPrefix")
    private String defaultPrefix = "!";

    @Value("displayConfiguration")
    private String displayConfiguration = "nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration";

    @Value("usageBuilder")
    private String usageBuilder = "nz.pumbas.halpbot.commands.usage.TypeUsageBuilder";

    @Value("useRoleBinding")
    private boolean useRoleBinding;
}
