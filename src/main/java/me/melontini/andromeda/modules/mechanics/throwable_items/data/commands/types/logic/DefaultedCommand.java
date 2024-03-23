package me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.types.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.melontini.andromeda.common.util.MiscUtil;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.Context;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorData;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.Command;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.commands.CommandType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter @Accessors(fluent = true)
public class DefaultedCommand extends Command {

    public static final Codec<DefaultedCommand> CODEC = RecordCodecBuilder.create(data -> data.group(
            MiscUtil.listCodec(CommandType.DISPATCH).fieldOf("commands").forGetter(DefaultedCommand::commands),
            MiscUtil.listCodec(CommandType.DISPATCH).fieldOf("default").forGetter(DefaultedCommand::defaultCommands),
            MiscUtil.LOOT_CONDITION_CODEC.optionalFieldOf("condition").forGetter(Command::getCondition)
    ).apply(data, DefaultedCommand::new));

    private final List<Command> commands;
    private final List<Command> defaultCommands;

    public DefaultedCommand(List<Command> commands, List<Command> defaultCommands, Optional<LootCondition> condition) {
        super(Collections.emptyList(), ItemBehaviorData.Particles.EMPTY, condition);
        this.commands = commands;
        this.defaultCommands = defaultCommands;
    }

    @Override
    public boolean execute(Context context) {
        if (!this.condition.map(condition1 -> condition1.test(context.lootContext())).orElse(true)) return false;

        boolean b = false;
        for (Command command : commands) {
            b |= command.execute(context);
        }
        if (!b) {
            for (Command defaultCommand : defaultCommands) {
                b |= defaultCommand.execute(context);
            }
            return b;
        }
        return true;
    }

    @Override
    protected @Nullable ServerCommandSource createSource(Context context) {
        return null;
    }

    @Override
    public CommandType type() {
        return CommandType.DEFAULTED;
    }
}
