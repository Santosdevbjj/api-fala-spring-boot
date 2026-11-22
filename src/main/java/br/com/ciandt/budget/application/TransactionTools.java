package br.com.ciandt.budget.application;

import br.com.ciandt.budget.domain.Category;
import br.com.ciandt.budget.domain.Transaction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

/** Ferramentas limitadas que permitem ao modelo consultar e registrar despesas. */
@Component
public class TransactionTools {
    private final CreateTransactionUseCase create;
    private final ListTransactionsUseCase list;
    public TransactionTools(CreateTransactionUseCase create, ListTransactionsUseCase list) {
        this.create = create; this.list = list;
    }

    @Tool(description = "Registra uma despesa informada pela pessoa. amountCents é o valor inteiro em centavos (R$ 60,00 = 6000). Use apenas quando a pessoa afirmar que gastou ou pagou.")
    public Transaction registerExpense(String description, long amountCents, Category category) {
        return create.execute(description, amountCents, category == null ? Category.OTHER : category);
    }

    @Tool(description = "Lista despesas registradas, opcionalmente filtradas por categoria GROCERIES, FARMA, AUTO ou OTHER. Use para responder consultas sobre gastos já salvos.")
    public List<Transaction> listExpenses(Category category) { return list.execute(category); }
}
