package com.lcstudio.metzremote.ir

/**
 * Boutons de la télécommande + code par défaut (NEC 32 bits, notation MSB first,
 * base Skyworth/TCL/Coocaa : préfixe 0x7070). null = à trouver avec le scanner.
 */
enum class IrButton(val label: String, val default: Long?) {
    POWER("Power", 0x707030CFL),
    SOURCE("Source", null),
    MUTE("Muet", 0x7070B04FL),
    HOME("Home", null),
    YOUTUBE("YouTube", null),
    BACK("Retour", 0x7070DD22L),
    UP("Haut", 0x707042BDL),
    DOWN("Bas", 0x7070C23DL),
    LEFT("Gauche", 0x707022DDL),
    RIGHT("Droite", 0x7070A25DL),
    OK("OK", 0x7070629DL),
    VOL_UP("Volume +", 0x707028D7L),
    VOL_DOWN("Volume -", 0x7070A857L),
    CH_UP("Chaîne +", 0x707048B7L),
    CH_DOWN("Chaîne -", 0x7070C837L),
}
