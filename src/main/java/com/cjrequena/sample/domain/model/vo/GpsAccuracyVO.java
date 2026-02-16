package com.cjrequena.sample.domain.model.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Accuracy value object (GPS accuracy in meters).
 */
@Getter
@Builder
@Jacksonized
@EqualsAndHashCode
public class GpsAccuracyVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final BigDecimal meters;

    private GpsAccuracyVO(BigDecimal meters) {
        if (meters == null || meters.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("GPS Accuracy must be non-negative");
        }
        this.meters = meters.setScale(2, RoundingMode.HALF_UP);
    }

    public static GpsAccuracyVO of(BigDecimal meters) {
        return new GpsAccuracyVO(meters);
    }

    public static GpsAccuracyVO of(double meters) {
        return new GpsAccuracyVO(BigDecimal.valueOf(meters));
    }

    public boolean isHighAccuracy() {
        return meters.doubleValue() <= 10.0;
    }

    public boolean isMediumAccuracy() {
        return meters.doubleValue() > 10.0 && meters.doubleValue() <= 50.0;
    }

    public boolean isLowAccuracy() {
        return meters.doubleValue() > 50.0;
    }

    @Override
    public String toString() {
        return "±" + meters + " m";
    }
}
