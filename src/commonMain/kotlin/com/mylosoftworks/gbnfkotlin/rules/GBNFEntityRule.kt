package com.mylosoftworks.com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.com.mylosoftworks.gbnfkotlin.entries.GBNFEntity

class GBNFEntityRule(val entity: GBNFEntity): GBNFRule() {
    override fun compile(): String {
        return entity.identifier!!
    }
}