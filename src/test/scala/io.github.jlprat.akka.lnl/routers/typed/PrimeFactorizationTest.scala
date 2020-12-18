package io.github.jlprat.akka.lnl.routers.typed

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class PrimeFactorizationTest extends AnyFlatSpec with Matchers {

  "PrimeFactorization" should "calculate prime factors with 2 prime factors" in {
    PrimeFactorization.primeFactors(6, Seq.empty) shouldBe Seq(2, 3)
    PrimeFactorization.primeFactors(9493538, Seq.empty) shouldBe Seq(2, 4746769)
    PrimeFactorization.primeFactors(4934578352334L, Seq.empty) shouldBe Seq(2, 3, 59, 71, 196330801)
  }

  it should "calculate prime factors of already prime numbers" in {
    PrimeFactorization.primeFactors(5209, Seq.empty) shouldBe Seq(5209)
  }

  it should "calculate prime factors of primes with same prime all over" in {
    PrimeFactorization.primeFactors(16, Seq.empty) shouldBe Seq(2, 2, 2, 2)
  }

  it should "calculate prime factors of primes with more than 2 different prime factors" in {
    PrimeFactorization.primeFactors(663, Seq.empty) shouldBe Seq(3, 13, 17)
    PrimeFactorization.primeFactors(125231066, Seq.empty) shouldBe Seq(2, 7907, 7919)
  }
}
