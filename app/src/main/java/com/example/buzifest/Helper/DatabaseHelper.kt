package com.example.buzifest.Helper

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.example.buzifest.Data.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private  const val DATABASE_NAME = "buzivest.db"
        private  const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createPortfolioTableQuery = "CREATE TABLE portfolios(id TEXT PRIMARY KEY, storeName TEXT NOT NULL, address TEXT NOT NULL, province TEXT NOT NULL, image TEXT NOT NULL, storeType TEXT NOT NULL, logo TEXT NOT NULL, fundingTarget INTEGER NOT NULL, description TEXT NOT NULL, publicShareStock REAL NOT NULL, dividendPayoutPeriod INTEGER NOT NULL, mainShareHolder TEXT NOT NULL, publisher TEXT NOT NULL, grossProfit INTEGER NOT NULL)"
        val createUserPortfolioTable = "CREATE TABLE userPortfolios (id TEXT PRIMARY KEY, userEmail TEXT NOT NULL, portfolioID TEXT NOT NULL, purchaseAmount INTEGER NOT NULL, earnings INTEGER NOT NULL, FOREIGN KEY (portfolioID) REFERENCES portfolios(id))"
        val createNewsTable = "CREATE TABLE news (id TEXT PRIMARY KEY, newsLinkUrl TEXT NOT NULL, newsTitle TEXT NOT NULL, newsImageUrl TEXT NOT NULL)"
        val createUpcomingPortfolioTable = "CREATE TABLE upcomingPortfolios (id TEXT PRIMARY KEY, storeName TEXT NOT NULL, storeImage TEXT NOT NULL, logo TEXT NOT NULL, daysLeft INTEGER NOT NULL)"
        db?.execSQL(createPortfolioTableQuery)
        db?.execSQL(createUserPortfolioTable)
        db?.execSQL(createNewsTable)
        db?.execSQL(createUpcomingPortfolioTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropNewsTableQuery = "DROP TABLE IF EXISTS news"
        val dropTransactionTableQuery = "DROP TABLE IF EXISTS portfolios"
        val dropDollTableQuery = "DROP TABLE IF EXISTS userPortfolios"
        val dropUpcomingPortfolioTableQuery = "DROP TABLE IF EXISTS upcomingPortfolios"
        db?.execSQL(dropNewsTableQuery)
        db?.execSQL(dropTransactionTableQuery)
        db?.execSQL(dropDollTableQuery)
        db?.execSQL(dropUpcomingPortfolioTableQuery)
        onCreate(db)
    }

    fun insertUpcomingPortfolio(upcomingPortfolio: UpcomingPortfolio){
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", upcomingPortfolio.id)
            put("storeName", upcomingPortfolio.storeName)
            put("storeImage", upcomingPortfolio.storeImage)
            put("logo", upcomingPortfolio.logo)
            put("daysLeft", upcomingPortfolio.daysLeft)
        }
        db.insert("upcomingPortfolios",null, values)
        db.close();
    }

    fun insertPortfolio(portfolio: Portfolio) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", portfolio.id)
            put("storeName", portfolio.storeName)
            put("address", portfolio.address)
            put("province", portfolio.province)
            put("image", portfolio.image)
            put("storeType", portfolio.storeType)
            put("logo", portfolio.logo)
            put("fundingTarget", portfolio.fundingTarget)
            put("description", portfolio.description)
            put("publicShareStock", portfolio.publicShareStock)
            put("dividendPayoutPeriod", portfolio.dividendPayoutPeriod)
            put("mainShareHolder", portfolio.mainShareHolder)
            put("publisher", portfolio.publisher)
            put("grossProfit", portfolio.grossProfit)
        }
        db.insert("portfolios",null, values)
        db.close();
    }

    fun insertUserPortfolios(userPortfolio: UserPortfolio) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", userPortfolio.id)
            put("portfolioID", userPortfolio.portfolioID)
            put("userEmail", userPortfolio.userEmail)
            put("purchaseAmount", userPortfolio.purchaseAmount)
            put("earnings", userPortfolio.earnings)
        }
        db.insert("userPortfolios",null, values)
        db.close();
    }

    fun insertNews(news:News) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", news.id)
            put("newsLinkUrl", news.newsLinkUrl)
            put("newsTitle", news.newsTitle)
            put("newsImageUrl", news.newsImageUrl)
        }
        db.insert("news",null, values)
        db.close()
    }

    fun checkUserPortfolioExist(currentPortfolioID: String, currentEmail: String):UserPortfolio {
        val db = readableDatabase
        val query = "SELECT * FROM userPortfolios"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val portfolioID = cursor.getString(cursor.getColumnIndexOrThrow("portfolioID"))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow("userEmail"))
            val purchaseAmount = cursor.getInt(cursor.getColumnIndexOrThrow("purchaseAmount"))
            val earnings = cursor.getInt(cursor.getColumnIndexOrThrow("earnings"))
            if(portfolioID == currentPortfolioID && userEmail == currentEmail) {
                return UserPortfolio(id, userEmail, portfolioID, purchaseAmount, earnings);
            }
        }
        cursor.close()
        db.close()
        return UserPortfolio("","","",0,0)
    }

    fun selectAllUpcomingPortfolio():ArrayList<UpcomingPortfolio> {
        val db = readableDatabase
        val upcomingPortfolioList:ArrayList<UpcomingPortfolio> = arrayListOf()
        val query = "SELECT * from upcomingPortfolios"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val storeImage = cursor.getString(cursor.getColumnIndexOrThrow("storeImage"))
            val storeName = cursor.getString(cursor.getColumnIndexOrThrow("storeName"))
            val logo = cursor.getString(cursor.getColumnIndexOrThrow("logo"))
            val daysLeft = cursor.getInt(cursor.getColumnIndexOrThrow("daysLeft"))
            upcomingPortfolioList.add(UpcomingPortfolio(id, storeName, storeImage, logo, daysLeft))
        }
        cursor.close()
        db.close()
        return upcomingPortfolioList
    }

    @SuppressLint("SuspiciousIndentation")
    fun selectTrendingPortfolio():ArrayList<TrendingPortfolio> {
        val db = readableDatabase
        val portfolioList:ArrayList<TrendingPortfolio> = ArrayList()
        val query = "SELECT p.*, COUNT(up.id) AS investorCount, COALESCE(SUM(up.purchaseAmount), 0) AS totalInvested FROM portfolios p LEFT JOIN userPortfolios up ON p.id = up.portfolioID GROUP BY p.id ORDER BY investorCount DESC"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val storeName = cursor.getString(cursor.getColumnIndexOrThrow("storeName"))
            val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val province = cursor.getString(cursor.getColumnIndexOrThrow("province"))
            val image = cursor.getString(cursor.getColumnIndexOrThrow("image"))
            val logo = cursor.getString(cursor.getColumnIndexOrThrow("logo"))
            val storeType = cursor.getString(cursor.getColumnIndexOrThrow("storeType"))
            val fundingTarget = cursor.getInt(cursor.getColumnIndexOrThrow("fundingTarget"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val publicShareStock = cursor.getDouble(cursor.getColumnIndexOrThrow("publicShareStock"))
            val dividendPayoutPeriod = cursor.getInt(cursor.getColumnIndexOrThrow("dividendPayoutPeriod"))
            val mainShareHolder = cursor.getString(cursor.getColumnIndexOrThrow("mainShareHolder"))
            val publisher = cursor.getString(cursor.getColumnIndexOrThrow("publisher"))
            val grossProfit = cursor.getInt(cursor.getColumnIndexOrThrow("grossProfit"))
            val totalInvested = cursor.getInt(cursor.getColumnIndexOrThrow("totalInvested"))
            val investorCount = cursor.getInt(cursor.getColumnIndexOrThrow("investorCount"))
            val tempPortfolio = TrendingPortfolio(Portfolio(id, storeName, address, province, image, storeType, logo, fundingTarget, description, publicShareStock, dividendPayoutPeriod, mainShareHolder, publisher, grossProfit), totalInvested, investorCount)

                portfolioList.add(tempPortfolio)
        }
        cursor.close()
        db.close()
        return portfolioList
    }

    fun selectSpecificUserPortfolioAmount(currentPortfolioID: String):Int{
        val db = readableDatabase
        val query = "SELECT purchaseAmount FROM userPortfolios WHERE id = ?"
        var purchaseAmount = 0
        val cursor = db.rawQuery(query, arrayOf(currentPortfolioID))
        while(cursor.moveToNext()) {
            purchaseAmount = cursor.getInt(cursor.getColumnIndexOrThrow("purchaseAmount")).toInt()
        }
        cursor.close()
        db.close()
        return purchaseAmount
    }


    fun updateUserPortfolio(currentID: String, amount:Int): Boolean  {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("purchaseAmount", amount)
        return try {
            val success = db.update("userPortfolios", contentValues, "id = ?", arrayOf(currentID))
            success != -1
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun selectAllPortfolios():ArrayList<Portfolio>{
        val db = readableDatabase
        val portfolioList:ArrayList<Portfolio> = ArrayList()
        val query = "SELECT * FROM portfolios"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val storeName = cursor.getString(cursor.getColumnIndexOrThrow("storeName"))
            val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val province = cursor.getString(cursor.getColumnIndexOrThrow("province"))
            val image = cursor.getString(cursor.getColumnIndexOrThrow("image"))
            val logo = cursor.getString(cursor.getColumnIndexOrThrow("logo"))
            val storeType = cursor.getString(cursor.getColumnIndexOrThrow("storeType"))
            val fundingTarget = cursor.getInt(cursor.getColumnIndexOrThrow("fundingTarget"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val publicShareStock = cursor.getDouble(cursor.getColumnIndexOrThrow("publicShareStock"))
            val dividendPayoutPeriod = cursor.getInt(cursor.getColumnIndexOrThrow("dividendPayoutPeriod"))
            val mainShareHolder = cursor.getString(cursor.getColumnIndexOrThrow("mainShareHolder"))
            val publisher = cursor.getString(cursor.getColumnIndexOrThrow("publisher"))
            val grossProfit = cursor.getInt(cursor.getColumnIndexOrThrow("grossProfit"))
            val tempPortfolio = Portfolio(id, storeName, address, province, image, storeType, logo, fundingTarget, description, publicShareStock, dividendPayoutPeriod, mainShareHolder, publisher, grossProfit)
            portfolioList.add(tempPortfolio)
        }
        cursor.close()
        db.close()
        return portfolioList
    }

    fun selectAllUserPortfolios():ArrayList<UserPortfolio>{
        val db = readableDatabase
        val userPortfolioList:ArrayList<UserPortfolio> = ArrayList()
        val query = "SELECT * FROM userPortfolios"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val portfolioID = cursor.getString(cursor.getColumnIndexOrThrow("portfolioID"))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow("userEmail"))
            val purchaseAmount = cursor.getInt(cursor.getColumnIndexOrThrow("purchaseAmount"))
            val earnings = cursor.getInt(cursor.getColumnIndexOrThrow("earnings"))
            val tempUserPortfolio = UserPortfolio(id,userEmail,portfolioID,purchaseAmount,earnings)
            userPortfolioList.add(tempUserPortfolio)
        }
        cursor.close()
        db.close()
        return userPortfolioList
    }



    fun selectTopNews():ArrayList<News> {
        val db = readableDatabase
        val newsList:ArrayList<News> = ArrayList()
        val query = "SELECT * FROM news LIMIT 5"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val newsLinkUrl = cursor.getString(cursor.getColumnIndexOrThrow("newsLinkUrl"))
            val newsTitle = cursor.getString(cursor.getColumnIndexOrThrow("newsTitle"))
            val newsImageUrl = cursor.getString(cursor.getColumnIndexOrThrow("newsImageUrl"))
            val tempNews = News(id, newsLinkUrl, newsTitle, newsImageUrl)
            newsList.add(tempNews)
        }
        cursor.close()
        db.close()
        return newsList
    }

    fun selectAllNews():ArrayList<News> {
        val db = readableDatabase
        val newsList:ArrayList<News> = ArrayList()
        val query = "SELECT * FROM news"
        val cursor = db.rawQuery(query, null)
        while(cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val newsLinkUrl = cursor.getString(cursor.getColumnIndexOrThrow("newsLinkUrl"))
            val newsTitle = cursor.getString(cursor.getColumnIndexOrThrow("newsTitle"))
            val newsImageUrl = cursor.getString(cursor.getColumnIndexOrThrow("newsImageUrl"))
            val tempNews = News(id, newsLinkUrl, newsTitle, newsImageUrl)
            newsList.add(tempNews)
        }
        cursor.close()
        db.close()
        return newsList
    }

    fun selectPurchaseAmountOfPortfolio(portfolioID:String): PortfolioSummary {
        val db = readableDatabase
        var totalInvestor = 0
        var totalInvested = 0
        val queryInvestor = "SELECT SUM(purchaseAmount) AS total from userPortfolios WHERE portfolioID = ?"
        val queryInvested = "SELECT COUNT(DISTINCT userEmail) AS total from userPortfolios WHERE portfolioID = ?"
        val cursorInvestor = db.rawQuery(queryInvestor, arrayOf(portfolioID))
        while (cursorInvestor.moveToNext()) {
            totalInvestor = cursorInvestor.getInt(cursorInvestor.getColumnIndexOrThrow("total"))
        }
        val cursorInvested = db.rawQuery(queryInvested, arrayOf(portfolioID))
        while (cursorInvested.moveToNext()) {
            totalInvested = cursorInvested.getInt(cursorInvested.getColumnIndexOrThrow("total"))
        }
        cursorInvestor.close()
        cursorInvested.close()
        db.close()
        return PortfolioSummary(totalInvestor, totalInvested)
    }

    fun selectUserSummaryValue(userEmail:String):ValueSummaryData{
        val db = readableDatabase
        var totalValue = 0
        var totalResult = 0
        val queryTotalValue = "SELECT SUM(purchaseAmount) AS totalValue from userPortfolios WHERE userEmail = ?"
        val queryTotalResult = "SELECT SUM(earnings) AS totalValue from userPortfolios WHERE userEmail = ?"
        val cursorValue = db.rawQuery(queryTotalValue, arrayOf(userEmail))
        while (cursorValue.moveToNext()) {
            totalValue = cursorValue.getInt(cursorValue.getColumnIndexOrThrow("totalValue"))
        }
        val cursorResult = db.rawQuery(queryTotalResult, arrayOf(userEmail))
        while (cursorResult.moveToNext()) {
            totalResult = cursorResult.getInt(cursorResult.getColumnIndexOrThrow("totalValue"))
        }
        cursorValue.close()
        cursorResult.close()
        db.close()
        return ValueSummaryData(totalValue, totalResult)
    }



    fun selectSpecificUserPortfolio(portfolioID:String):UserPortfolio {
        val db = readableDatabase
        lateinit var userPortfolio:UserPortfolio
        val query = "SELECT * from userPortfolios WHERE id = ?"
        val cursorValue = db.rawQuery(query, arrayOf(portfolioID))
        while (cursorValue.moveToNext()) {
            val id = cursorValue.getString(cursorValue.getColumnIndexOrThrow("id"))
            val portfolioID = cursorValue.getString(cursorValue.getColumnIndexOrThrow("portfolioID"))
            val userEmail = cursorValue.getString(cursorValue.getColumnIndexOrThrow("userEmail"))
            val purchaseAmount = cursorValue.getInt(cursorValue.getColumnIndexOrThrow("purchaseAmount"))
            val earnings = cursorValue.getInt(cursorValue.getColumnIndexOrThrow("earnings"))

            userPortfolio = UserPortfolio(id, userEmail, portfolioID, purchaseAmount, earnings)
        }
        cursorValue.close()
        db.close()
        return userPortfolio
    }

    fun selectUserPortfolio(email:String):ArrayList<UserPortfolio> {
        val db = readableDatabase
        val userPortfolioList:ArrayList<UserPortfolio> = ArrayList()
        val query = "SELECT * from userPortfolios WHERE userEmail = ?"
        val cursorValue = db.rawQuery(query, arrayOf(email))
        while (cursorValue.moveToNext()) {
            val id = cursorValue.getString(cursorValue.getColumnIndexOrThrow("id"))
            val portfolioID = cursorValue.getString(cursorValue.getColumnIndexOrThrow("portfolioID"))
            val userEmail = cursorValue.getString(cursorValue.getColumnIndexOrThrow("userEmail"))
            val purchaseAmount = cursorValue.getInt(cursorValue.getColumnIndexOrThrow("purchaseAmount"))
            val earnings = cursorValue.getInt(cursorValue.getColumnIndexOrThrow("earnings"))

            userPortfolioList.add(UserPortfolio(id, userEmail, portfolioID, purchaseAmount, earnings))
        }
        cursorValue.close()
        db.close()
        return userPortfolioList
    }


    fun selectFulfilledUserPortfolio(currentEmail:String):ArrayList<UserPortfolio>{
        val userPortfolios = selectUserPortfolio(currentEmail)
        val fulfilledUserPortfolios:ArrayList<UserPortfolio> = arrayListOf()
        for (userPortfolio in userPortfolios) {
            val portfolio = selectSpecificPortfolio(userPortfolio.portfolioID)
            val totalInvested = selectPurchaseAmountOfPortfolio(userPortfolio.portfolioID).totalInvested
            if(totalInvested >= portfolio.fundingTarget) {
                fulfilledUserPortfolios.add(userPortfolio)
            }
        }
        return fulfilledUserPortfolios
    }

    fun selectUserPortfoliosPortofolio(email:String):ArrayList<Portfolio> {
        val userPortfolioList = selectUserPortfolio(email)
        val portfolioList:ArrayList<Portfolio> = arrayListOf()
        for(userPortfolio in userPortfolioList) {
            val portfolio = selectSpecificPortfolio(userPortfolio.portfolioID)
            val totalInvested = selectPurchaseAmountOfPortfolio(userPortfolio.portfolioID).totalInvested
            println("${portfolio.fundingTarget} | ${totalInvested}")
            if(totalInvested < portfolio.fundingTarget) {
                portfolioList.add(portfolio)
            }
        }
        return portfolioList
    }

    fun clearUserEarnings(currentEmail: String){
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("earnings", 0)
        try {
            val success = db.update("userPortfolios", contentValues, "userEmail = ?", arrayOf(currentEmail))
            println("hmm: ${success}")
            success != -1
        } catch (e: Exception) {
            println("hmm: ${e}")
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun selectSpecificPortfolio(portfolioID: String):Portfolio {
        val db = readableDatabase
        val query = "SELECT * from portfolios WHERE id = ?"
        lateinit var tempPortfolio: Portfolio
        val cursor = db.rawQuery(query, arrayOf(portfolioID))

        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val storeName = cursor.getString(cursor.getColumnIndexOrThrow("storeName"))
            val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val province = cursor.getString(cursor.getColumnIndexOrThrow("province"))
            val image = cursor.getString(cursor.getColumnIndexOrThrow("image"))
            val logo = cursor.getString(cursor.getColumnIndexOrThrow("logo"))
            val storeType = cursor.getString(cursor.getColumnIndexOrThrow("storeType"))
            val fundingTarget = cursor.getInt(cursor.getColumnIndexOrThrow("fundingTarget"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val publicShareStock = cursor.getDouble(cursor.getColumnIndexOrThrow("publicShareStock"))
            val dividendPayoutPeriod = cursor.getInt(cursor.getColumnIndexOrThrow("dividendPayoutPeriod"))
            val mainShareHolder = cursor.getString(cursor.getColumnIndexOrThrow("mainShareHolder"))
            val publisher = cursor.getString(cursor.getColumnIndexOrThrow("publisher"))
            val grossProfit = cursor.getInt(cursor.getColumnIndexOrThrow("grossProfit"))
            tempPortfolio = Portfolio(id, storeName, address, province, image, storeType, logo, fundingTarget, description, publicShareStock, dividendPayoutPeriod, mainShareHolder, publisher, grossProfit)
        }
        cursor.close()
        db.close()
        return tempPortfolio
    }



    fun clearDatabase() {
        val db = writableDatabase
        // Clear the database
        db.execSQL("DELETE FROM portfolios")
        db.execSQL("DELETE FROM userPortfolios")
        db.execSQL("DELETE FROM news")
        db.execSQL("DELETE FROM upcomingPortfolios")
        println("deleted!!")
        // Close the database connection
        db.close()
    }

}