package com.example.pokedex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.model.Pokemon

class PokemonAdapter(
    private var pokemons: List<Pokemon>,
    private val onItemClick: (Pokemon) -> Unit // Função de callback para o clique
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tvPokemonName)
        val tvTipo: TextView = itemView.findViewById(R.id.tvPokemonType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemons[position]
        holder.tvNome.text = pokemon.nome
        holder.tvTipo.text = "Tipo: ${pokemon.tipo}"

        holder.itemView.setOnClickListener {
            onItemClick(pokemon)
        }
    }

    override fun getItemCount(): Int = pokemons.size

    fun updateList(newPokemons: List<Pokemon>) {
        pokemons = newPokemons
        notifyDataSetChanged()
    }
}