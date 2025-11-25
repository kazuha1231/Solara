package com.defendersofsolara.core;

public enum TargetType {
    SELF,           // Target only self
    SINGLE_ENEMY,   // Target one enemy (player must select)
    SINGLE_ALLY,    // Target one ally (player must select)
    ALL_ENEMIES,    // Target all enemies (auto)
    ALL_ALLIES,     // Target all allies (auto)
    RANDOM_ENEMY,   // Random enemy (AI)
    RANDOM_ALLY     // Random ally (AI)
}
