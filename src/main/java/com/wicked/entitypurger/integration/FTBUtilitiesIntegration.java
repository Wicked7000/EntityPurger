package com.wicked.entitypurger.integration;

import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.minecraft.util.math.ChunkPos;

import java.util.Objects;

public class FTBUtilitiesIntegration {
    public static final String MOD_ID = "ftbutilities";

    public boolean isClaimedChunk(ChunkPos chunkPos, int dimension){
        ChunkDimPos chunkDimPos = new ChunkDimPos(chunkPos, dimension);
        ClaimedChunk claimedChunk = ClaimedChunks.instance.getChunk(chunkDimPos);
        return Objects.nonNull(claimedChunk);
    }
}
