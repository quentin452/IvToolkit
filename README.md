# IvToolkit Arcana Rpg Continuation Edition

This mod is a fork of https://legacy.curseforge.com/minecraft/mc-mods/ivtoolkit

Even if the mod is described has "Arcana Rpg Continuation Edition" you can use it without Arcana Rpg Continuation

To see all listed changes : https://github.com/quentin452/IvToolkit/wiki

# Links to descriptions of my projects.

[*Github*](https://github.com/quentin452/IvToolkit)

//[*Curseforge*](https://legacy.curseforge.com/minecraft/mc-mods/recurrent-complex-arcana-rpg-continuation-edition)

# Discord

Add me on discord : iamacatfr

Discord server : https://discord.gg/ZnmHKJzKkZ

**New Fixes :**

Fix crash when exporting large structures due to buffer overflow from recurrent complex

**New Optimizations:**

Optimised block data packing algorithm
The block data packing routine was rewritten to use a ByteBuffer instead of a direct byte array. This has the following benefits:
The buffer size is correctly calculated based on the data length instead of using a fixed max size
Data is written incrementally to the buffer to avoid out of memory issues
No risk of buffer overflows thanks to ByteBuffer's automatic size management
Packing result is retrieved into a final byte array once complete
This resolves the ArrayIndexOutOfBounds exceptions occurring for very large block collections and improves the robustness and efficiency of the packing.
