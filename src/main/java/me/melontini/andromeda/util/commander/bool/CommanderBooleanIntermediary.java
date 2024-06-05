package me.melontini.andromeda.util.commander.bool;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.ToString;
import me.melontini.commander.api.expression.BooleanExpression;
import net.minecraft.loot.context.LootContext;

import java.util.function.Supplier;

@ToString
public final class CommanderBooleanIntermediary implements BooleanIntermediary {

    public static final Codec<CommanderBooleanIntermediary> CODEC = BooleanExpression.CODEC.xmap(CommanderBooleanIntermediary::new, CommanderBooleanIntermediary::getExpression);

    @Getter
    private final BooleanExpression expression;
    private final boolean constant;


    public CommanderBooleanIntermediary(BooleanExpression expression) {
        this.expression = expression;
        this.constant = expression.toSource().left().isPresent();
    }

    @Override
    public boolean asBoolean(Supplier<LootContext> supplier) {
        return this.expression.asBoolean(constant ? null : supplier.get());
    }
}