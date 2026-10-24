# Clockwork modpack

A .mrpack needs download URLs + hashes, which I can't fetch offline, so generate it with packwiz after building the jar:

    packwiz init                       # pick Fabric + your MC version
    packwiz modrinth add fabric-api
    packwiz modrinth add sodium        # optional performance
    packwiz modrinth add modmenu       # optional
    packwiz refresh
    # put build/libs/clockwork-0.1.0.jar in mods/ (or host it and use `packwiz url add`)
    packwiz modrinth export            # -> .mrpack importable in Modrinth App / Prism
Clockwork itself has no hard dependency besides Fabric API.
