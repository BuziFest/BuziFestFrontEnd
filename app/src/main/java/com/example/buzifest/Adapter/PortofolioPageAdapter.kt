package com.example.buzifest.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buzifest.Activity.FulfilledPortfolioDetail
import com.example.buzifest.Activity.PortfolioDetail
import com.example.buzifest.Data.Portfolio
import com.example.buzifest.Data.UserPortfolio
import com.example.buzifest.Helper.DatabaseHelper
import com.example.buzifest.Helper.formatNumber
import com.example.buzifest.R
import org.w3c.dom.Text

class PortofolioPageAdapter(private val userPortofolioList: List<UserPortfolio>, private val context: Context): RecyclerView.Adapter<PortofolioPageAdapter.ViewHolder>() {
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val portofolioPoint = itemView.findViewById<ImageView>(R.id.portofolio_recyler_point)
        val portfolioEarnings = itemView.findViewById<TextView>(R.id.portofolio_recyler_earnings)
        val portfolioValue = itemView.findViewById<TextView>(R.id.portofolio_recyler_totalValue)
        val portofolioLogo = itemView.findViewById<ImageView>(R.id.portofolio_recyler_logo)
        val portofolioName = itemView.findViewById<TextView>(R.id.portofolio_recycler_name)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.portofolio_custom_recycler, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userPortofolioList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sqlitedb = DatabaseHelper(context)
        val currentItem = userPortofolioList[position]
        val portfolio = sqlitedb.selectSpecificPortfolio(currentItem.portfolioID)
        holder.portfolioValue.text = "Rp ${formatNumber(currentItem.earnings+currentItem.purchaseAmount)}"
        holder.portofolioName.text = portfolio.storeName
        holder.portfolioEarnings.text = "Rp ${formatNumber(currentItem.earnings)}"
        holder.itemView.setOnClickListener {
            val intent = Intent(context, FulfilledPortfolioDetail::class.java).apply {
                putExtra("portfolioID", currentItem.portfolioID)
                putExtra("userPortfolioID", currentItem.id)
                putExtra("status", "fulfilled")
            }
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
        }
        holder.portofolioPoint
        Glide.with(holder.itemView.context)
            .load(portfolio.logo)
            .into(holder.portofolioLogo)
    }
}