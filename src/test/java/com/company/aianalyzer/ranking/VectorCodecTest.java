package com.company.aianalyzer.ranking;

import com.company.aianalyzer.exception.RankingDataException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorCodecTest {
    private final VectorCodec codec = new VectorCodec();

    @Test
    void decodesLittleEndianFloatVectors() {
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(1.5f).putFloat(-2.0f).array();
        assertThat(codec.decode(bytes, 2)).containsExactly(1.5f, -2.0f);
    }

    @Test
    void rejectsInvalidVectorLengths() {
        assertThatThrownBy(() -> codec.decode(new byte[3], 2)).isInstanceOf(RankingDataException.class);
    }
}
