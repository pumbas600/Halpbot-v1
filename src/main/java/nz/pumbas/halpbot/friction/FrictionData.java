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

    public void setExternalForceAtBlock(int blockIndex) {
        if (blockIndex >= this.blocks.size() || 0 > blockIndex)
            throw new ErrorMessageException(
                String.format("The index %s is out of bounds", blockIndex));

        this.externalForceBlockIndex = blockIndex;
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



    }
}
