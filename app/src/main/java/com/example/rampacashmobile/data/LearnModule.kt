package com.example.rampacashmobile.data

data class LearnModule(
    val id: String,
    val title: String,
    val bonkReward: Int,
    val submodules: List<Submodule>,
    val completedSubmodules: Set<String> = emptySet()
) {
    val progress: Float
        get() = if (submodules.isEmpty()) 0f else completedSubmodules.size.toFloat() / submodules.size.toFloat()

    val isCompleted: Boolean
        get() = submodules.isNotEmpty() && completedSubmodules.size == submodules.size
}

data class Submodule(
    val id: String,
    val title: String,
    val lessons: List<Lesson>
)

data class Lesson(
    val id: String,
    val title: String,
    val content: String,
    val type: LessonType = LessonType.TEXT
)

enum class LessonType {
    TEXT,
    QUIZ
}

object LearnModulesData {
    val modules = listOf(
        LearnModule(
            id = "basics_investing",
            title = "Basics of Investing",
            bonkReward = 50,
            submodules = listOf(
                Submodule(
                    id = "what_is_investing",
                    title = "What is Investing?",
                    lessons = listOf(
                        Lesson(
                            id = "investing_intro",
                            title = "Introduction to Investing",
                            content = """
                                **What is Investing?**
                                
                                Investing is the act of allocating money or resources with the expectation of generating income or profit over time.
                                
                                **Key Concepts:**
                                
                                • **Capital**: The money you use to invest
                                • **Returns**: The profit you make from your investments
                                • **Time Horizon**: How long you plan to invest
                                
                                When you invest, you're putting your money to work for you. Instead of just saving money in a bank account, investing allows your money to grow through various opportunities in the market.
                                
                                **Why Invest?**
                                
                                1. **Beat Inflation**: Keep your purchasing power over time
                                2. **Build Wealth**: Grow your money faster than traditional savings
                                3. **Reach Goals**: Achieve financial milestones like retirement or buying a home
                                4. **Generate Passive Income**: Earn money without active work
                            """.trimIndent()
                        ),
                        Lesson(
                            id = "how_investing_works",
                            title = "How Investing Works",
                            content = """
                                **How Does Investing Work?**
                                
                                Investing works by purchasing assets that have the potential to increase in value or generate income.
                                
                                **The Process:**
                                
                                1. **Choose an Investment**: Select stocks, bonds, real estate, etc.
                                2. **Purchase the Asset**: Buy shares or units of the investment
                                3. **Hold for Growth**: Wait for the asset to appreciate
                                4. **Earn Returns**: Receive dividends or sell for profit
                                
                                **Example:**
                                If you buy a stock for $100 and it grows to $120, you've made a 20% return on your investment.
                                
                                **Important Principle:**
                                The longer you invest, the more time your money has to grow through compound returns.
                            """.trimIndent()
                        ),
                        Lesson(
                            id = "risk_reward",
                            title = "Risk and Reward",
                            content = """
                                **Understanding Risk and Reward**
                                
                                In investing, risk and reward are directly related. Higher potential returns usually come with higher risk.
                                
                                **Risk Levels:**
                                
                                🟢 **Low Risk**: Savings accounts, government bonds
                                • Lower returns (1-3% annually)
                                • Very safe, minimal chance of loss
                                
                                🟡 **Medium Risk**: Corporate bonds, dividend stocks
                                • Moderate returns (4-8% annually)
                                • Some volatility but relatively stable
                                
                                🔴 **High Risk**: Growth stocks, cryptocurrencies
                                • Higher potential returns (10%+ annually)
                                • Significant volatility and potential for loss
                                
                                **Key Takeaway:**
                                Diversification helps manage risk while pursuing returns. Never invest money you can't afford to lose.
                            """.trimIndent()
                        )
                    )
                ),
                Submodule(
                    id = "types_investments",
                    title = "Types of Investments",
                    lessons = listOf(
                        Lesson(
                            id = "stocks",
                            title = "Stocks",
                            content = """
                                **What are Stocks?**
                                
                                Stocks represent ownership in a company. When you buy a stock, you own a small piece of that company.
                                
                                **How Stocks Work:**
                                
                                • Companies issue stocks to raise money
                                • Stock prices fluctuate based on company performance and market conditions
                                • You can profit through price appreciation or dividends
                                
                                **Types of Stocks:**
                                
                                📈 **Growth Stocks**: Companies expected to grow faster than the market
                                💰 **Dividend Stocks**: Companies that pay regular dividends
                                🔷 **Blue Chip Stocks**: Large, established, financially stable companies
                                
                                **Example:**
                                If you buy 10 shares of Apple at $150 each, you invest $1,500. If the price rises to $180, your shares are now worth $1,800 - a $300 profit!
                            """.trimIndent()
                        ),
                        Lesson(
                            id = "bonds",
                            title = "Bonds",
                            content = """
                                **What are Bonds?**
                                
                                Bonds are loans you make to governments or corporations. In return, they pay you interest over time.
                                
                                **How Bonds Work:**
                                
                                1. You lend money by buying a bond
                                2. The issuer pays you interest (coupon) regularly
                                3. At maturity, you get your principal back
                                
                                **Types of Bonds:**
                                
                                🏛️ **Government Bonds**: Very safe, lower returns
                                🏢 **Corporate Bonds**: Higher returns, moderate risk
                                🌎 **Municipal Bonds**: Tax-advantaged bonds from local governments
                                
                                **Benefits:**
                                • More stable than stocks
                                • Predictable income
                                • Capital preservation
                            """.trimIndent()
                        ),
                        Lesson(
                            id = "etfs_mutual_funds",
                            title = "ETFs & Mutual Funds",
                            content = """
                                **ETFs and Mutual Funds**
                                
                                Both are investment vehicles that pool money from many investors to buy a diversified portfolio.
                                
                                **Exchange-Traded Funds (ETFs):**
                                
                                • Trade like stocks on exchanges
                                • Usually track an index (e.g., S&P 500)
                                • Lower fees
                                • Tax efficient
                                
                                **Mutual Funds:**
                                
                                • Actively managed by professionals
                                • Bought/sold at end of trading day
                                • Higher fees
                                • May outperform or underperform the market
                                
                                **Why Choose Them?**
                                
                                ✅ Instant diversification
                                ✅ Professional management
                                ✅ Lower minimum investment
                                ✅ Easier than picking individual stocks
                            """.trimIndent()
                        )
                    )
                ),
                Submodule(
                    id = "investment_goals",
                    title = "Setting Investment Goals",
                    lessons = listOf(
                        Lesson(
                            id = "smart_goals",
                            title = "SMART Investment Goals",
                            content = """
                                **Setting SMART Goals**
                                
                                Use the SMART framework to set effective investment goals:
                                
                                **S - Specific**
                                Be clear about what you want to achieve
                                ❌ "I want to be rich"
                                ✅ "I want to save $50,000 for a house down payment"
                                
                                **M - Measurable**
                                Track your progress with numbers
                                ✅ "Save $500 per month"
                                
                                **A - Achievable**
                                Set realistic goals based on your income
                                ✅ "Invest 10% of my income"
                                
                                **R - Relevant**
                                Align with your life priorities
                                ✅ "Build retirement fund for financial security"
                                
                                **T - Time-bound**
                                Set a deadline
                                ✅ "Reach $50,000 in 5 years"
                            """.trimIndent()
                        ),
                        Lesson(
                            id = "time_horizons",
                            title = "Investment Time Horizons",
                            content = """
                                **Understanding Time Horizons**
                                
                                Your investment strategy should match when you'll need the money.
                                
                                **Short-Term (0-3 years)**
                                
                                💡 Goals: Emergency fund, vacation, car
                                📊 Investments: Savings accounts, money market funds, short-term bonds
                                ⚠️ Priority: Safety and liquidity over growth
                                
                                **Medium-Term (3-10 years)**
                                
                                💡 Goals: House down payment, child's education
                                📊 Investments: Balanced portfolio of stocks and bonds
                                ⚠️ Priority: Balance of growth and stability
                                
                                **Long-Term (10+ years)**
                                
                                💡 Goals: Retirement, wealth building
                                📊 Investments: Primarily stocks and growth assets
                                ⚠️ Priority: Maximum growth potential
                                
                                **Remember:** Longer time horizons allow you to take more risk for potentially higher returns.
                            """.trimIndent()
                        ),
                        Lesson(
                            id = "starting_investing",
                            title = "How to Get Started",
                            content = """
                                **Getting Started with Investing**
                                
                                Follow these steps to begin your investment journey:
                                
                                **Step 1: Build an Emergency Fund**
                                Save 3-6 months of expenses before investing
                                This ensures you won't need to sell investments in emergencies
                                
                                **Step 2: Pay Off High-Interest Debt**
                                Credit card debt often costs more than investment returns
                                Focus on this before investing aggressively
                                
                                **Step 3: Define Your Goals**
                                Use the SMART framework we learned
                                Know your time horizon and risk tolerance
                                
                                **Step 4: Choose Your Account**
                                • Brokerage account for flexibility
                                • Retirement account (401k, IRA) for tax benefits
                                • Apps like Rampa for easy access to tokenized stocks
                                
                                **Step 5: Start Small**
                                You don't need thousands to start
                                Begin with what you can afford consistently
                                
                                **Step 6: Automate Your Investments**
                                Set up automatic monthly contributions
                                This removes emotion and builds discipline
                                
                                🎉 **Congratulations!** You've completed the basics of investing!
                            """.trimIndent()
                        )
                    )
                )
            )
        ),
        LearnModule(
            id = "risk_management",
            title = "Risk Management",
            bonkReward = 50,
            submodules = listOf(
                Submodule(
                    id = "understanding_risk",
                    title = "Understanding Risk vs Reward",
                    lessons = listOf()
                ),
                Submodule(
                    id = "risk_assessment",
                    title = "Risk Assessment Tools",
                    lessons = listOf()
                ),
                Submodule(
                    id = "portfolio_risk",
                    title = "Portfolio Risk Management",
                    lessons = listOf()
                )
            )
        ),
        LearnModule(
            id = "diversification",
            title = "Diversification",
            bonkReward = 50,
            submodules = listOf(
                Submodule(
                    id = "asset_diversification",
                    title = "Asset Class Diversification",
                    lessons = listOf()
                ),
                Submodule(
                    id = "geographic_diversification",
                    title = "Geographic Diversification",
                    lessons = listOf()
                ),
                Submodule(
                    id = "time_diversification",
                    title = "Time Diversification",
                    lessons = listOf()
                )
            )
        )
    )
}

