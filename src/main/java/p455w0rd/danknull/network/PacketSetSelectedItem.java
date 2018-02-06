package p455w0rd.danknull.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.danknull.blocks.tiles.TileDankNullDock;
import p455w0rd.danknull.init.ModItems;
import p455w0rd.danknull.util.DankNullUtils;

/**
 * @author p455w0rd
 *
 */
public class PacketSetSelectedItem implements IMessage {

	private int index;
	private BlockPos pos = null;

	@Override
	public void fromBytes(ByteBuf buf) {
		index = buf.readInt();
		if (buf.readBoolean()) {
			int x = buf.readInt();
			int y = buf.readInt();
			int z = buf.readInt();
			pos = new BlockPos(x, y, z);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(index);
		if (pos != null) {
			buf.writeBoolean(true);
			buf.writeInt(pos.getX());
			buf.writeInt(pos.getY());
			buf.writeInt(pos.getZ());
		}
		else {
			buf.writeBoolean(false);
		}
	}

	public PacketSetSelectedItem() {
	}

	public PacketSetSelectedItem(int index, BlockPos pos) {
		this.index = index;
		this.pos = pos;
	}

	public PacketSetSelectedItem(int index) {
		this(index, null);
	}

	public static class Handler implements IMessageHandler<PacketSetSelectedItem, IMessage> {
		@Override
		public IMessage onMessage(PacketSetSelectedItem message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				handle(message, ctx);
			});
			return null;
		}

		private void handle(PacketSetSelectedItem message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			ItemStack dankNull = ItemStack.EMPTY;
			if (player != null) {
				if (message.pos == null) {
					if (player.getHeldItemMainhand().getItem() == ModItems.DANK_NULL) {
						dankNull = player.getHeldItemMainhand();
					}
					else if (player.getHeldItemOffhand().getItem() == ModItems.DANK_NULL) {
						dankNull = player.getHeldItemOffhand();
					}
				}
				else {
					if (player.getEntityWorld().getTileEntity(message.pos) != null) {
						TileEntity te = player.getEntityWorld().getTileEntity(message.pos);
						if (te instanceof TileDankNullDock) {
							dankNull = ((TileDankNullDock) te).getStack();
						}
					}
				}
				DankNullUtils.setSelectedStackIndex(DankNullUtils.getNewDankNullInventory(dankNull), message.index, player.getEntityWorld(), message.pos);
			}
		}
	}

}
