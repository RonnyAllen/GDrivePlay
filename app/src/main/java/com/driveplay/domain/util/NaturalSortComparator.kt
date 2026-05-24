package com.driveplay.domain.util

object NaturalSortComparator : Comparator<String> {
    override fun compare(a: String, b: String): Int {
        var ia = 0
        var ib = 0
        while (ia < a.length && ib < b.length) {
            val ca = a[ia]
            val cb = b[ib]
            if (ca.isDigit() && cb.isDigit()) {
                var na = 0L
                var nb = 0L
                while (ia < a.length && a[ia].isDigit()) {
                    na = na * 10 + (a[ia++] - '0')
                }
                while (ib < b.length && b[ib].isDigit()) {
                    nb = nb * 10 + (b[ib++] - '0')
                }
                if (na != nb) return na.compareTo(nb)
            } else {
                val cmp = ca.lowercaseChar().compareTo(cb.lowercaseChar())
                if (cmp != 0) return cmp
                ia++
                ib++
            }
        }
        return a.length - b.length
    }
}
