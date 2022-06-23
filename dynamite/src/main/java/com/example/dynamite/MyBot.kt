package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import com.softwire.dynamite.game.Round
import java.lang.Integer.max

class MyBot : Bot {

    private val NUMBER_OF_MOVES: Int = 5
    private val DECAY = 0.9
    private val MAX_DYNAMITE = 100

    private var dynCount: Int = 0

    private val moveKeys: Map<Move, Int> = mapOf(Move.R to 0, Move.P to 1, Move.S to 2, Move.W to 3, Move.D to 4)
    private val keyMoves: Map<Int, Move> = mapOf(0 to Move.R, 1 to Move.P, 2 to Move.S, 3 to Move.W, 4 to Move.D)
    private val beatMove: Map<Move, Move> = mapOf(Move.R to Move.P, Move.P to Move.S, Move.S to Move.R, Move.D to Move.W, Move.W to Move.R)

    var moveProb: Array<Array<Array<Double>>> = Array(NUMBER_OF_MOVES) { Array(NUMBER_OF_MOVES) { Array(NUMBER_OF_MOVES) { 0.0 } } }

    private fun getMoveProb(rounds: List<Round>) {
        for(k in 0 until rounds.size - 1) {
            val currentRound: Round = rounds[k]
            val nextRound: Round = rounds[k + 1]

            val key1: Int = moveKeys[currentRound.getP1()]!!
            val key2: Int = moveKeys[currentRound.getP2()]!!

            val actualMoveKey: Int = moveKeys[nextRound.getP2()]!!

            for (q in 0 until NUMBER_OF_MOVES) {
                moveProb[key1][key2][q] *= DECAY
            }

            moveProb[key1][key2][actualMoveKey] += 1.0
        }
    }

    private fun getRandomMove(): Move{
        return listOf(Move.R, Move.S, Move.P).shuffled().first()
    }

    override fun makeMove(gamestate: Gamestate): Move {
        // Are you debugging?
        // Put a breakpoint in this method to see when we make a move
        if(gamestate.rounds.size < 2){
            return getRandomMove()
        }

        getMoveProb(rounds=gamestate.rounds)

        val prevRound = gamestate.rounds.last()

        val key1: Int = moveKeys[prevRound.getP1()]!!
        val key2: Int = moveKeys[prevRound.getP2()]!!

        var max = 0.0
        var min = 10.0
        var maxMove = 0

        for (q in 0 until NUMBER_OF_MOVES) {
            if(moveProb[key1][key2][q] > max) {
                max = moveProb[key1][key2][q]
                maxMove = q
            }
            if(moveProb[key1][key2][q] < min) {
                min = moveProb[key1][key2][q]
            }
        }

        var dynChance = 17
        var dontLike = false

        if(max - min < 0.3) {
            dynChance *= 2
            dontLike = true
        }

        if(dynCount < MAX_DYNAMITE && (1..dynChance).shuffled().first() == 1) {
            dynCount += 1
            return Move.D
        }

        if(dontLike) {
            return getRandomMove()
        }


        val opMove = keyMoves[maxMove]

        return beatMove[opMove]!!
    }

    init {
        // Are you debugging?
        // Put a breakpoint on the line below to see when we start a new match
        println("Started new match")
    }
}