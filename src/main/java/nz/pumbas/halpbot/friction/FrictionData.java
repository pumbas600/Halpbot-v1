package nz.pumbas.halpbot.friction;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

import nz.pumbas.commands.Exceptions.ErrorMessageException;
import nz.pumbas.utilities.Utilities;

public class FrictionData
{
    private final List<BlockData> blocks;
    private double angleRad;
    private int externalForceBlockIndex = -1;
    private boolean isExternalForceActingUpwards;

    public FrictionData() {
        this.blocks = new ArrayList<>();
    }

    public void addBlock(BlockData block) {
        this.blocks.add(block);
    }

    public void setAngle(double angleDeg) {
        if (0 > angleDeg || Utilities.quarterRotation <= angleDeg)
            throw new ErrorMessageException(
                String.format("The angle %s must be between 0 and 90 degrees", angleDeg));

        this.angleRad = Math.toRadians(angleDeg);
    }

    public void setExternalForceActingUpwards() {
        this.isExternalForceActingUpwards = true;
    }

    public void setExternalForceAtBlock(int blockIndex) {
        if (blockIndex > this.blocks.size() || 1 > blockIndex)
            throw new ErrorMessageException(
                String.format("The index %s is out of bounds", blockIndex));

        this.externalForceBlockIndex = blockIndex - 1;
    }

    public boolean hasAllData() {
        return 1 < this.blocks.size() && -1 != this.externalForceBlockIndex;
    }

    public void solve(MessageReceivedEvent event)
    {
        if (!this.hasAllData())
            throw new ErrorMessageException("You're missing some data necessary to perform this action");

        double totalWeight = 0;
        for (BlockData block : this.blocks) {
            totalWeight += block.getWeight();

            double normal = totalWeight * Math.cos(this.angleRad);
            block.setNormal(normal);
        }

        int weightForceDirection = this.isExternalForceActingUpwards ? 1 : -1;

        String result;
        //External force is at the second to top block
        if (1 == this.externalForceBlockIndex)
            result = this.externalForceIsAtTop(weightForceDirection);

        else if (this.blocks.size() - 1 == this.externalForceBlockIndex)
            result = this.externalForceIsAtBottom(weightForceDirection);

        else
            result = this.externalForceIsInMiddle(weightForceDirection);

        event.getChannel().sendMessage(result).queue();
    }

    private String externalForceIsAtTop(int weightForceDirection) {
        double frictionTop = this.blocks.get(0).getMaxFriction();
        double weightTotal = 0;
        double minimumExternalForce = Double.MAX_VALUE;
        int minimumExternalForceBlockIndex = -1;

        for (int blockIndex = 1; blockIndex < this.blocks.size(); blockIndex++) {
            BlockData block = this.blocks.get(blockIndex);
            weightTotal += block.getWeight();
            double frictionBottom = block.getMaxFriction();

            double externalForce =
                frictionTop + frictionBottom + weightForceDirection * weightTotal * Math.sin(this.angleRad);
            if (externalForce < minimumExternalForce) {
                minimumExternalForce = externalForce;
                minimumExternalForceBlockIndex = blockIndex;
            }
        }

        if (1 == minimumExternalForceBlockIndex)
            return String.format("The minimum force required is %.4fN and will cause the 2nd block from the top to " +
                    "slide out", minimumExternalForce);
        return String.format("The minimum force required is %.4fN and will cause the 2nd to %sth blocks from the top " +
            "to slide out", minimumExternalForce, minimumExternalForceBlockIndex + 1);
    }

    private String externalForceIsAtBottom(int weightForceDirection) {
        int bottomIndex = this.blocks.size() - 1;

        double frictionBottom = this.blocks.get(bottomIndex).getMaxFriction();
        double weightTotal = 0;
        double minimumExternalForce = Double.MAX_VALUE;
        int minimumExternalForceBlockIndex = -1;

        for (int blockIndex = bottomIndex; 0 < blockIndex; blockIndex--) {
            weightTotal += this.blocks.get(blockIndex).getWeight();
            double frictionTop = this.blocks.get(blockIndex - 1).getMaxFriction();

            double externalForce =
                frictionTop + frictionBottom + weightForceDirection * weightTotal * Math.sin(this.angleRad);
            if (externalForce < minimumExternalForce) {
                minimumExternalForce = externalForce;
                minimumExternalForceBlockIndex = blockIndex;
            }
        }

        if (bottomIndex == minimumExternalForceBlockIndex)
            return String.format("The minimum force required is %.4fN and will cause the bottom block to slide out",
                minimumExternalForce);
        return String.format("The minimum force required is %.4fN and will cause the 1st to %sth blocks from the " +
            "bottom to slide out", minimumExternalForce, this.blocks.size() - minimumExternalForceBlockIndex);
    }

    private String externalForceIsInMiddle(int weightForceDirection) {
        throw new UnsupportedOperationException("Unfortunately, the external force can only be applied to the bottom " +
            "block or the second to top block. Solving for the external force if its somewhere in the middle is still" +
            " under development");
    }
}
