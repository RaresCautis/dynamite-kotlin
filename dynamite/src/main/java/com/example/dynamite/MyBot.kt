package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import com.softwire.dynamite.game.Round
import java.lang.Integer.max

class MyBot : Bot {

    private val NUMBER_OF_MOVES: Int = 5
    private val baseDecay = 0.9
    private var DECAY = 0.9

    private var dynCount: Int = 0

    private val moveKeys: Map<Move, Int> = mapOf(Move.R to 0, Move.P to 1, Move.S to 2, Move.W to 3, Move.D to 4)
    private val keyMoves: Map<Int, Move> = mapOf(0 to Move.R, 1 to Move.P, 2 to Move.S, 3 to Move.W, 4 to Move.D)
    private val beatMove: Map<Move, Move> = mapOf(Move.R to Move.P, Move.P to Move.S, Move.S to Move.R, Move.D to Move.W, Move.W to Move.R)

    var moveProb: Array<Array<Array<Double>>> = Array(NUMBER_OF_MOVES) { Array(NUMBER_OF_MOVES) { Array(NUMBER_OF_MOVES) { 0.0 } } }

    private fun moveToKey(move: Move): Int {
        return moveKeys[move]!!
    }

    private fun getMoveProb(rounds: List<Round>) {
        var dynMatter = false

        var consecDynCount = 0

        for(k in max(0, rounds.size - 100) until rounds.size - 1) {
            val currentRound: Round = rounds[k]
            val nextRound: Round = rounds[k + 1]

            val move1 = currentRound.getP1()
            val move2 = currentRound.getP2()

            if(nextRound.getP2() == Move.D && move2 == Move.D){
                consecDynCount++
            }
            else consecDynCount = 0

            if(consecDynCount >= 3)
                dynMatter = true

            if((move1 != Move.D && move2 != Move.D) || dynMatter) {

                for (p in 0 until NUMBER_OF_MOVES) {
                    for (r in 0 until NUMBER_OF_MOVES) {
                        for (q in 0 until NUMBER_OF_MOVES) {
                            moveProb[p][r][q] *= DECAY
                        }
                    }
                }

                val key1: Int = moveToKey(move1)
                val key2: Int = moveToKey(move2)

                val actualMoveKey: Int = moveToKey(nextRound.getP2())

                moveProb[key1][key2][actualMoveKey] += 1.0
            }
        }
    }

    private fun getRandomMove(): Move{
        return listOf(Move.R, Move.S, Move.P).shuffled().first()
    }

    private fun checkLost(round: Round): Boolean{
        val moveMe = round.getP1()
        val moveOp = round.getP2()

        if(moveMe == Move.W){
            if(moveOp != Move.D)
                return true
        }

        return beatMove[moveMe] == moveOp
    }

    override fun makeMove(gamestate: Gamestate): Move {
        // Are you debugging?
        // Put a breakpoint in this method to see when we make a move

        getMoveProb(rounds=gamestate.rounds)

        var max = 0.0
        var min = 5000.0
        var maxMove = 0

        if(gamestate.rounds.size < 2){
            return getRandomMove()
        }/*

        if(gamestate.rounds.size < 10){
            return Move.D
        }*/

        /*if(gamestate.rounds.size == 10){
            decideDynBehaviour
        }*/

        val prevRound = gamestate.rounds.last()

        if(checkLost(prevRound)){
            DECAY *= baseDecay
        }
        else DECAY = baseDecay

        val key1: Int = moveToKey(prevRound.getP1())
        val key2: Int = moveToKey(prevRound.getP2())

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
            dynChance = 32
            dontLike = true
        }

        if(dynCount < 100 && (1..dynChance).shuffled().first() == 1) {
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