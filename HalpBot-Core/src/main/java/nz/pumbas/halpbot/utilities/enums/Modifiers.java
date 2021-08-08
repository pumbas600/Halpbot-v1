package nz.pumbas.halpbot.utilities.enums;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

public enum Modifiers
{
    PUBLIC(Modifier::isPublic),
    PROTECTED(Modifier::isProtected),
    PRIVATE(Modifier::isPrivate),
    STATIC(Modifier::isStatic),
    ABSTRACT(Modifier::isAbstract),
    FINAL(Modifier::isFinal);

    private final Predicate<Integer> modifierPredicate;

    Modifiers(Predicate<Integer> modifierPredicate) {
        this.modifierPredicate = modifierPredicate;
    }

    public boolean hasModifier(Member member) {
        return this.modifierPredicate.test(member.getModifiers());
    }

}
