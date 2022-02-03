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

package nz.pumbas.halpbot;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.testsuite.HartshornTest;

@Activator(scanPackages = "nz.pumbas.halpbot")
@HartshornTest
public class Tests
{
//    @InjectTest
//    public void logPriortityTest(LogDecoratorFactory factory) {
//        LogDecorator<?> decorator = factory.decorate(new HalpbotCommandInvokable(null, null),
//                new Log() {
//                    @Override
//                    public Class<? extends Annotation> annotationType() {
//                        return Log.class;
//                    }
//
//                    @Override
//                    public LogLevel value() {
//                        return LogLevel.DEBUG;
//                    }
//                });
//
//        Assertions.assertNotNull(decorator);
//        Assertions.assertInstanceOf(CustomLogDecorator.class, decorator);
//    }
}
