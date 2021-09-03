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
