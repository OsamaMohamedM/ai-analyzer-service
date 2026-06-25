package com.company.aianalyzer.ranking;

import com.company.aianalyzer.exception.RankingDataException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Component
public class VectorCodec {
    public float[] decode(byte[] bytes, int dimension) {
        if (bytes == null || bytes.length != dimension * Float.BYTES) {
            throw new RankingDataException("Stored embedding has an invalid byte length");
        }
        float[] vector = new float[dimension];
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < dimension; i++) vector[i] = buffer.getFloat();
        return vector;
    }

    public double dot(float[] left, float[] right) {
        double result = 0.0;
        for (int i = 0; i < left.length; i++) result += left[i] * right[i];
        return result;
    }
}
