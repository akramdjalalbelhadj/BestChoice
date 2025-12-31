package fr.amu.bestchoice.web.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper config
 *
 * componentModel = "spring" : Le mapper sera un bean Spring (injection possible)
 *
 * unmappedTargetPolicy = ReportingPolicy.ERROR :
 * IMPORTANT ! Si on oublie un champ, la compilation échoue
 *
 * nullValuePropertyMappingStrategy.IGNORE :
 * Si un champ DTO est null, on ne modifie pas l'entité
 */

@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapStructConfig {
}
