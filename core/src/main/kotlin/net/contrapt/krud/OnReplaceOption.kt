package net.contrapt.krud

/**
 * Options for _onReplace_ for associations
 */
enum class OnReplaceOption {
    raise,
    invalidate,
    nilify,
    update,
    delete
}