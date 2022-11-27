package io.github.ulisse1996.jaorm.vendor.postgre.spatial;

import io.github.ulisse1996.jaorm.annotation.ConverterProvider;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.spatial.Geography;
import org.postgresql.geometric.PGpolygon;

@ConverterProvider
public class GeographyConverter implements ValueConverter<PGpolygon, Geography> {

    @Override
    public Geography fromSql(PGpolygon val) {
        return null;
    }

    @Override
    public PGpolygon toSql(Geography val) {
        return null;
    }
}
