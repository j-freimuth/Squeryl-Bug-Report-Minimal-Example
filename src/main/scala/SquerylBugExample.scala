object SquerylBugExample extends App {

  Class.forName("org.h2.Driver")

  def connection: java.sql.Connection = {
    java.sql.DriverManager.getConnection("jdbc:h2:./squerl")
  }

  import org.squeryl._

  SessionFactory.concreteFactory = Option(() => Session.create(connection, new adapters.H2Adapter))

  import org.squeryl.annotations.Column
  case class Product(id: Long, name: String) extends KeyedEntity[Long]
  case class ProductCustomer(id: Long, @Column("product_id") productId: Long) extends KeyedEntity[Long]

  import PrimitiveTypeMode._

  object DB extends Schema {
    val product = table[Product]
    val productCustomer = table[ProductCustomer]
  }

  transaction {
    import DB._
    drop
    create
    printDdl

    product.insert(Product(1, "Product_1"))
    product.insert(Product(2, "Product_2"))
    productCustomer.insert(ProductCustomer(1, 1))


    val joined1 = join(product, productCustomer.leftOuter) { (product, productCustomer) =>

      select(product, productCustomer)
        .on(product.id === productCustomer.get.productId)
    }.toList

    /* Joining both tables directly works */
    println(joined1)

    val productCustomerQuery: Query[ProductCustomer] = from(productCustomer)(productCustomer => select(productCustomer))

    val joined2 = join(product, productCustomerQuery.leftOuter){ (product, productCustomer) =>

      where(product.id === 1)
      .select(product, productCustomer)
        .on(product.id === productCustomer.get.productId)
    }.toList

    /* With query works as long as no null values are encountered */
    println(joined2)

    val joined3 = join(product, productCustomerQuery.leftOuter){ (product, productCustomer) =>

      select(product, productCustomer)
        .on(product.id === productCustomer.get.productId)
    }.toList

    /* Throws a mapping error as soon as null values are encountered */
    println(joined3)
  }
}
