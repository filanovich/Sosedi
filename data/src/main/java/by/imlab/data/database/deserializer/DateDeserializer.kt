package by.imlab.data.database.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.NumericNode
import java.util.*

class DateDeserializer : StdDeserializer<Date>(Date::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Date {
        val node = p?.codec?.readTree<TreeNode>(p)
        return Date((node as NumericNode).asLong() * 1000L)
    }

}