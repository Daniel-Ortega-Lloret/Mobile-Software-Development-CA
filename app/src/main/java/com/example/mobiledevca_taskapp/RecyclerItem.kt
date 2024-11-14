package com.example.mobiledevca_taskapp

class RecyclerItem constructor(var Card: Tasks)
{
    // private fields of the class
    private var _card: Tasks = Card

    fun get_name(): String
    {
        return _card.get_name()
    }

    fun get_description(): String
    {
        return _card.get_description()
    }
}