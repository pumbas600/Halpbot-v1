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

import java.util.ArrayList;
import java.util.List;

import nz.pumbas.halpbot.commands.annotations.Command;

public class NumberSystemConverters
{
    @Command(alias = "logBin", description = "Converts a decimal number into its binary equivalent using logs")
    public String logBin(double decimalNumber) {
        StringBuilder result = new StringBuilder();
        boolean addedDp = false;
        int currentOneIndex = (int)Math.floor(Math.log(decimalNumber)/Math.log(2));
        int newOneIndex = currentOneIndex;
        int interations = 0;
        while (0 != decimalNumber && interations < 30) {
            if (1 > decimalNumber && !addedDp) {
                result.append('.');
                addedDp = true;
            }

            int zeros = currentOneIndex - newOneIndex - 1;
            result.append("0".repeat(Math.max(0, zeros)));
            result.append('1');
            currentOneIndex = newOneIndex;

            decimalNumber -= Math.pow(2, currentOneIndex);
            interations++;
            newOneIndex = (int)Math.floor(Math.log(decimalNumber)/Math.log(2));
        }
        return result.toString();
    }


    @Command(alias = "decToBin", description = "Converts a decimal number into its binary equivalent")
    public String decToBin(double decimalNumber) {
        StringBuilder resultBuilder = new StringBuilder();
        int decIntegerComponent = (int)decimalNumber;
        int tempIntegerComponent = decIntegerComponent;

        List<Integer> integerNumbers = new ArrayList<>();
        int remainder;
        int placement = 0;
        while (0 != tempIntegerComponent && 20 > placement) {
            remainder = tempIntegerComponent % 2;
            tempIntegerComponent /= 2;
            integerNumbers.add(remainder);
            placement++;
        }

        for (int i = integerNumbers.size() - 1; 0 <= i; i--) {
            resultBuilder.append(integerNumbers.get(i));
        }

        //If there is a decimal component
        if (decimalNumber != decIntegerComponent) {
            resultBuilder.append('.');
            double decDecimalComponent = decimalNumber - decIntegerComponent;
            int iteration = 0;
            while (0 != decDecimalComponent && 10 > iteration) {
                decDecimalComponent *= 2;
                int integerComponent = (int)decDecimalComponent;
                decDecimalComponent -= integerComponent;
                iteration++;

                resultBuilder.append(integerComponent);
            }
        }

        return resultBuilder.toString();
    }

    public long decToBin(long decimalNumber) {
        long binaryResult = 0;
        long remainder;
        int placement = 0;
        while (0 != decimalNumber && 20 > placement) {
            remainder = decimalNumber % 2;
            decimalNumber /= 2;
            binaryResult += remainder * Math.pow(10, placement);
            placement++;
        }

        return binaryResult;
    }

    public double decToHex(double decimalNumber) {
        return -1;
    }
}
