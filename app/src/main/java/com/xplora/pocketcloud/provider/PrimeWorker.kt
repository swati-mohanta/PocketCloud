package com.xplora.pocketcloud.provider

object PrimeWorker {

    fun countPrimes(start: Int, end: Int): Long {
        var count = 0L
        for (n in start..end) {
            if (isPrime(n)) count++
        }
        return count
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        if (n == 2) return true
        if (n % 2 == 0) return false

        var i = 3
        while (i * i <= n) {
            if (n % i == 0) return false
            i += 2
        }
        return true
    }
}