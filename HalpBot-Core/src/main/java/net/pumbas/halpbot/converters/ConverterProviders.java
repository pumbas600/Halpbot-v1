package net.pumbas.halpbot.converters;

import net.pumbas.halpbot.converters.annotations.UseConverters;
import net.pumbas.halpbot.converters.parametercontext.HalpbotParameterAnnotationContext;
import net.pumbas.halpbot.converters.parametercontext.HalpbotParameterAnnotationService;
import net.pumbas.halpbot.converters.parametercontext.ParameterAnnotationContext;
import net.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;
import net.pumbas.halpbot.converters.tokens.HalpbotParsingToken;
import net.pumbas.halpbot.converters.tokens.HalpbotPlaceholderToken;
import net.pumbas.halpbot.converters.tokens.HalpbotTokenService;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.converters.tokens.PlaceholderToken;
import net.pumbas.halpbot.converters.tokens.TokenService;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
@RequiresActivator(UseConverters.class)
public class ConverterProviders {

    @Provider
    public Class<? extends ConverterHandler> converterHandler() {
        return HalpbotConverterHandler.class;
    }

    @Provider
    public Class<? extends TokenService> tokenService() {
        return HalpbotTokenService.class;
    }

    @Provider
    public Class<? extends ParameterAnnotationService> parameterAnnotationService() {
        return HalpbotParameterAnnotationService.class;
    }

    @Provider
    public Class<? extends ParsingToken> parsingToken() {
        return HalpbotParsingToken.class;
    }

    @Provider
    public Class<? extends PlaceholderToken> placeholderToken() {
        return HalpbotPlaceholderToken.class;
    }

    @Provider
    public Class<? extends ParameterAnnotationContext> parameterAnnotationContext() {
        return HalpbotParameterAnnotationContext.class;
    }
}
