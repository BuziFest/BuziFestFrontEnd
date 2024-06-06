package com.example.buzifest.Helper

import android.content.Context
import android.provider.ContactsContract.Data
import com.example.buzifest.Data.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

val db = FirebaseFirestore.getInstance()

suspend fun getUserFromFirestoreByEmail(email: String): User? {
    if (email.isEmpty()) {
        return User("", "", "", "", "", "", 0, 0, "")
    }

    val docRef = db.collection("users").document(email)

    return try {
        val document = docRef.get().await()
        if (document.exists()) {
            val userData = document.data
            if (userData != null) {
                val username = (userData["username"] as? String).orEmpty()
                val phoneNumber = (userData["phoneNumber"] as? String).orEmpty()
                val firstName = (userData["firstName"] as? String).orEmpty()
                val lastName = (userData["lastName"] as? String).orEmpty()
                val idCardNumber = (userData["idCardNumber"] as? String).orEmpty()
                val address = (userData["address"] as? String).orEmpty()
                val balance = (userData["balance"] as? Number)?.toInt() ?: 0
                val asset = (userData["asset"] as? Number)?.toInt() ?: 0
                User(username, firstName, lastName, email, idCardNumber, phoneNumber, balance, asset, address)
            } else {
                null // No data retrieved
            }
        } else {
            null // Document does not exist
        }
    } catch (e: Exception) {
        println("Error getting document: $e")
        null // Error occurred
    }
}

//Add New Portfolio
public fun addPortfolio(portfolio: Portfolio, context: Context){
    val value = hashMapOf(
        "id" to portfolio.id,
        "storeName" to portfolio.storeName,
        "address" to portfolio.address,
        "province" to portfolio.province,
        "imageUrl" to portfolio.image,
        "storeType" to portfolio.storeType,
        "logoUrl" to portfolio.logo,
        "fundingTarget" to portfolio.fundingTarget,
        "description" to portfolio.description,
        "publicShareStock" to portfolio.publicShareStock,
        "dividendPayoutPeriod" to portfolio.dividendPayoutPeriod,
        "mainShareHolder" to portfolio.mainShareHolder,
        "publisher" to portfolio.publisher,
        "grossProfit" to portfolio.grossProfit,
    )
    val sqliteDB = DatabaseHelper(context = context)
    sqliteDB.insertPortfolio(portfolio)

    db.collection("portfolios").document(portfolio.id)
        .set(value)
        .addOnSuccessListener {
            // Successfully added user to Firestore
        }
        .addOnFailureListener { e ->
            // Failed to add user to Firestore
            print(e)
        }
}

public fun changeUserAsset(amount:Int, email:String) {
    db.collection("users").document(email).update("asset", amount).addOnSuccessListener {
        println("User email successfully updated!")
    }.addOnFailureListener { e ->
        println("Error updating user email: $e")
    }
}

public fun addUserPortfolio(userPortfolio: UserPortfolio, context: Context) {
    val value = hashMapOf(
        "id" to userPortfolio.id,
        "email" to userPortfolio.userEmail,
        "portfolioID" to userPortfolio.portfolioID,
        "purchaseAmount" to userPortfolio.purchaseAmount,
        "totalProfit" to userPortfolio.totalProfit
    )
    val sqliteDB = DatabaseHelper(context = context)
    sqliteDB.insertUserPortfolios(userPortfolio)
    db.collection("userPortfolios").document(userPortfolio.id)
        .set(value)
        .addOnSuccessListener {
            // Successfully added user to Firestore
        }
        .addOnFailureListener { e ->
            // Failed to add user to Firestore
            print(e)
        }
}

public fun addNews(news: News, context: Context) {
    val sqliteDB = DatabaseHelper(context = context)
    val value = hashMapOf(
        "id" to news.id,
        "newsTitle" to news.newsTitle,
        "newsImageUrl" to news.newsImageUrl,
        "newsLinkUrl" to news.newsLinkUrl
    )
    sqliteDB.insertNews(news)

    db.collection("news").document(news.id)
        .set(value)
        .addOnSuccessListener {
            // Successfully added user to Firestore
        }
        .addOnFailureListener { e ->
            // Failed to add user to Firestore
            print(e)
        }
}

suspend fun getNewsData(context: Context): List<News> {
    val db = FirebaseFirestore.getInstance()
    val news = mutableListOf<News>()
    val sqliteDB = DatabaseHelper(context = context)
    return try {
        val documents = db.collection("news").get().await()
        for (document in documents) {
            val id = (document["id"] as? String).orEmpty()
            val newsLinkUrl = (document["newsLinkUrl"] as? String).orEmpty()
            val newsImageUrl = (document["newsImageUrl"] as? String).orEmpty()
            val newsTitle = (document["newsTitle"] as? String).orEmpty()
            val tempNews = News(id, newsLinkUrl, newsTitle, newsImageUrl)
            news.add(tempNews)
            sqliteDB.insertNews(tempNews)
        }
        news
    } catch (e: Exception) {
        println("Error getting documents: $e")
        emptyList()
    }
}

suspend fun getPortfoliosData(context: Context): List<Portfolio> {
    val db = FirebaseFirestore.getInstance()
    val portfolios = mutableListOf<Portfolio>()
    val sqliteDB = DatabaseHelper(context = context)
    return try {
        val documents = db.collection("portfolios").get().await()
        for (document in documents) {
            val storeType = (document["storeType"] as? String).orEmpty()
            val address = (document["address"] as? String).orEmpty()
            val province = (document["province"] as? String).orEmpty()
            val imageUrl = (document["imageUrl"] as? String).orEmpty()
            val publisher = (document["publisher"] as? String).orEmpty()
            val logoUrl = (document["logoUrl"] as? String).orEmpty()
            val description = (document["description"] as? String).orEmpty()
            val storeName = (document["storeName"] as? String).orEmpty()
            val publicShareStock = (document["publicShareStock"] as? Double) ?: 0.0
            val id = (document["id"] as? String).orEmpty()
            val mainShareHolder = (document["mainShareHolder"] as? String).orEmpty()
            val dividendPayoutPeriod = (document["dividendPayoutPeriod"] as? Number)?.toInt() ?: 0
            val fundingTarget = (document["fundingTarget"] as? Number)?.toInt() ?: 0
            val grossProfit = (document["grossProfit"] as? Number)?.toInt() ?: 0
            val data = Portfolio(id, storeName, address, province, imageUrl, storeType, logoUrl ,fundingTarget, description, publicShareStock, dividendPayoutPeriod, mainShareHolder, publisher, grossProfit)
            portfolios.add(data)
            sqliteDB.insertPortfolio(data)
        }
        portfolios
    } catch (e: Exception) {
        println("Error getting documents: $e")
        emptyList()
    }
}

suspend fun getAllUserPortfoliosData(context: Context): List<UserPortfolio> {
    val userPortfolios = mutableListOf<UserPortfolio>()
    val sqliteDB = DatabaseHelper(context = context)
    return try {
        val documents = db.collection("userPortfolios").get().await()
        for (document in documents.documents) {
            val id = (document["id"] as? String).orEmpty()
            val email = (document["email"] as? String).orEmpty()
            val portfolioID = (document["portfolioID"] as? String).orEmpty()
            val purchaseAmount = (document["purchaseAmount"] as? Number)?.toInt() ?: 0
            val totalProfit = (document["totalProfit"] as? Number)?.toInt() ?: 0
            val data = UserPortfolio(id, email, portfolioID, purchaseAmount, totalProfit)
            sqliteDB.insertUserPortfolios(data)

            userPortfolios.add(data)
        }
        userPortfolios
    } catch (e: Exception) {
        println("Error getting documents: $e")
        emptyList()
    }
}

suspend fun getAllPurchaseAmountOfPortfolio(portfolioID:String): PortfolioSummary {
    val documents = db.collection("userPortfolios").get().await()
    var total = 0
    var count = 0
    return try {
        for (document in documents.documents) {
            val id = document["portfolioID"] as? String
            val purchaseAmount = (document["purchaseAmount"] as? Number)?.toInt() ?: 0
            if(id == portfolioID) {
                total+=purchaseAmount
                count++
            }
        }
        PortfolioSummary(total,count)
    } catch (e: Exception) {
        println("Error getting documents: $e")
        PortfolioSummary(0,0)
    }
}

suspend fun getAllCurrentUserPortfoliosData(currentEmail:String): List<UserPortfolio> {
    val userPortfolios = mutableListOf<UserPortfolio>()

    return try {
        val documents = db.collection("userPortfolios").get().await()
        for (document in documents.documents) {
            val email = (document["email"] as? String).orEmpty()
            if(email == currentEmail) {
                val id = (document["id"] as? String).orEmpty()
                val portfolioID = (document["portfolioID"] as? String).orEmpty()
                val purchaseAmount = (document["purchaseAmount"] as? Number)?.toInt() ?: 0
                val totalProfit = (document["totalProfit"] as? Number)?.toInt() ?: 0
                val data = UserPortfolio(id, email, portfolioID, purchaseAmount, totalProfit)
                userPortfolios.add(data)
            }
        }
        userPortfolios
    } catch (e: Exception) {
        println("Error getting documents: $e")
        emptyList()
    }
}

suspend fun getUserPortfoliosPortofolio(currentEmail:String):List<Portfolio>{
    val userPortfolios = mutableListOf<UserPortfolio>()
    val portfolioList = mutableListOf<Portfolio>()
    return try {
        val documents = db.collection("userPortfolios").get().await()
        for (document in documents.documents) {
            val email = (document["email"] as? String).orEmpty()
            if(email == currentEmail) {
                val id = (document["id"] as? String).orEmpty()
                val portfolioID = (document["portfolioID"] as? String).orEmpty()
                val purchaseAmount = (document["purchaseAmount"] as? Number)?.toInt() ?: 0
                val totalProfit = (document["totalProfit"] as? Number)?.toInt() ?: 0
                val data = UserPortfolio(id, email, portfolioID, purchaseAmount, totalProfit)
                userPortfolios.add(data)
            }
        }
        println(userPortfolios)
        for(userPortfolio in userPortfolios) {
            val docRef = db.collection("portfolios").document(userPortfolio.portfolioID)
            val document = docRef.get().await()
            if(document.exists()) {
                val storeType = (document["storeType"] as? String).orEmpty()
                val address = (document["address"] as? String).orEmpty()
                val province = (document["province"] as? String).orEmpty()
                val imageUrl = (document["imageUrl"] as? String).orEmpty()
                val publisher = (document["publisher"] as? String).orEmpty()
                val logoUrl = (document["logoUrl"] as? String).orEmpty()
                val description = (document["description"] as? String).orEmpty()
                val storeName = (document["storeName"] as? String).orEmpty()
                val publicShareStock = (document["publicShareStock"] as? Double) ?: 0.0
                val id = (document["id"] as? String).orEmpty()
                val mainShareHolder = (document["mainShareHolder"] as? String).orEmpty()
                val dividendPayoutPeriod = (document["dividendPayoutPeriod"] as? Number)?.toInt() ?: 0
                val fundingTarget = (document["fundingTarget"] as? Number)?.toInt() ?: 0
                val grossProfit = (document["grossProfit"] as? Number)?.toInt() ?: 0
                val data = Portfolio(id, storeName, address, province, imageUrl, storeType, logoUrl ,fundingTarget, description, publicShareStock, dividendPayoutPeriod, mainShareHolder, publisher, grossProfit)
                portfolioList.add(data)
            }
        }
        portfolioList
    } catch (e: Exception) {
        println("Error getting documents: $e")
        emptyList()
    }
}

suspend fun getCurrentUserValueData(currentEmail:String): ValueSummaryData {
    var totalValue = 0
    var totalEarning = 0
    return try {
        val documents = db.collection("userPortfolios").get().await()
        for (document in documents.documents) {
            val email = (document["email"] as? String).orEmpty()
            if(email == currentEmail) {
                val purchaseAmount = (document["purchaseAmount"] as? Number)?.toInt() ?: 0
                val totalProfit = (document["totalProfit"] as? Number)?.toInt() ?: 0
                totalValue+=purchaseAmount
                totalEarning+=totalProfit
            }
        }
        println("value: ${totalValue} ${totalEarning}")
        ValueSummaryData(totalValue, totalEarning)
    } catch (e: Exception) {
        println("Error getting documents: $e")
        ValueSummaryData(0, 0)
    }
}
