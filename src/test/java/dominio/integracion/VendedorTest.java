package dominio.integracion;

import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dominio.GarantiaExtendida;
import dominio.Producto;
import dominio.Vendedor;
import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;
import dominio.repositorio.RepositorioProducto;
import persistencia.sistema.SistemaDePersistencia;
import testdatabuilder.ProductoTestDataBuilder;

public class VendedorTest {

	private static final String COMPUTADOR_LENOVO = "Computador Lenovo";
	private static final String NOMBRE_CLIENTE = "Ricardo Ayala Martínez";

	private SistemaDePersistencia sistemaPersistencia;

	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

	@Before
	public void setUp() {

		sistemaPersistencia = new SistemaDePersistencia();

		repositorioProducto = sistemaPersistencia.obtenerRepositorioProductos();
		repositorioGarantia = sistemaPersistencia.obtenerRepositorioGarantia();

		sistemaPersistencia.iniciar();
	}

	@After
	public void tearDown() {
		sistemaPersistencia.terminar();
	}

	/**
	 * Método que permite verificar que la garantía extendida se genera y se
	 * persiste correctamente.
	 * 
	 * Se verifica que el producto quede registrado con la nueva garantía, y que el
	 * nombre registrado en la garantía coincida con el enviado como parámetro
	 */
	@Test
	public void generarGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);

		// assert
		Assert.assertTrue(vendedor.tieneGarantia(producto.getCodigo()));
		Assert.assertNotNull(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo()));

		GarantiaExtendida garantiaExtendida = repositorioGarantia.obtener(producto.getCodigo());

		Assert.assertEquals(NOMBRE_CLIENTE, garantiaExtendida.getNombreCliente());

	}

	/**
	 * Método que permite verficar que el sistema no permita asociar una garantía a
	 * un producto que ya cuenta con una.
	 * 
	 * Se debe recibir una excepción con un mensaje de error que indica que el
	 * producto ya tiene garantía
	 */
	@Test
	public void productoYaTieneGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);

		try {

			vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);
			fail();

		} catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_TIENE_GARANTIA, e.getMessage());
		}
	}

	/**
	 * Método que permite verificar la obligatoriedad de los datos requeridos para
	 * la generación de garantías
	 * 
	 * El sistema debe generar una excepción indicando en un mensaje la
	 * obligatoriedad de los datos
	 */
	@Test
	public void faltanDatosObligatorios() {
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		try {
			vendedor.generarGarantia(null, "");
			fail();
		} catch (GarantiaExtendidaException e) {
			Assert.assertEquals(Vendedor.DATOS_OBLIGATORIOS, e.getMessage());
		}
	}

	/**
	 * Método que permite validar que a un producto que contenga 3 vocales en el
	 * código no se le pueda generar una garantía extendida
	 * 
	 * El sistema debe generar una excepción indicando en el mensaje que el producto
	 * no cuenta con garantía
	 */
	@Test
	public void productoNoCuentaConGarantiaExtendida() {
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo("a123ebI213").build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		try {
			vendedor.generarGarantia(producto.getCodigo(), NOMBRE_CLIENTE);
			fail();
		} catch (GarantiaExtendidaException e) {
			Assert.assertEquals(Vendedor.PRODUCTO_SIN_GARANTIA, e.getMessage());
		}
	}

	/**
	 * Método que permite verificar que se cumpla la regla de negocio para los
	 * productos con precios mayores a 50000. Se aplica 20% del precio para la
	 * garantía y 200 días sin contar los lunes. La fecha final no cae un domingo
	 * 
	 * El sistema registra la garantía para el producto, y se valida que el precio y
	 * la fecha de finalización coincidan con los esperados, y que el producto
	 * cuente con la nueva garantía
	 * 
	 * @throws ParseException En caso de error durante la creación de las fechas de
	 *                        prueba
	 */
	@Test
	public void garantiaExtendidaReglaVeintePorcientoDoscientosDias() throws ParseException {
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		Date fechaInicial = formatter.parse("16/08/2018");
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaInicial);
		double precioGarantia = vendedor.calcularPrecioGarantia(producto.getPrecio(), 0.2);
		Date fechaFin = vendedor.calcularFechaGaratia(fecha, producto.getPrecio());
		GarantiaExtendida garantiaExtendida = new GarantiaExtendida(producto, fechaInicial, fechaFin, precioGarantia,
				NOMBRE_CLIENTE);
		repositorioGarantia.agregar(garantiaExtendida);
		GarantiaExtendida garantia = repositorioGarantia.obtener(producto.getCodigo());
		// Se valida la garantia
		Assert.assertNotNull(garantia);
		// Se valida que el producto tenga garantía
		Assert.assertTrue(vendedor.tieneGarantia(producto.getCodigo()));
		// Se valida que el producto de la garantía coincida con el enviado para el
		// registro
		Assert.assertEquals(garantia.getProducto().getCodigo(), producto.getCodigo());
		// Se valida que el precio calculado sea igual al esperado
		Assert.assertTrue(garantia.getPrecioGarantia() == 156000.0);
		// Se valida la fecha de finalización
		Assert.assertTrue(formatter.format(garantia.getFechaFinGarantia()).equals("06/04/2019"));
		// Se valida el nombre del cliente
		Assert.assertTrue(garantia.getNombreCliente().equals(NOMBRE_CLIENTE));
	}

	/**
	 * Método que permite verificar que se cumpla la regla de negocio para los
	 * productos con precios mayores a 50000. Se aplica 20% del precio para la
	 * garantía y 200 días sin contar los lunes. La fecha final coincide con un
	 * domingo, por lo cual se debe asignar la fecha hasta el siguiente día habil
	 * 
	 * El sistema registra la garantía para el producto, y se valida que el precio y
	 * la fecha de finalización coincidan con los esperados, y que el producto
	 * cuente con la nueva garantía
	 * 
	 * @throws ParseException En caso de error durante la creación de las fechas de
	 *                        prueba
	 */
	@Test
	public void garantiaExtendidaReglaVeintePorcDoscientosDiasDomingo() throws ParseException {
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		Date fechaInicial = formatter.parse("17/08/2018");
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaInicial);
		double precioGarantia = vendedor.calcularPrecioGarantia(producto.getPrecio(), 0.2);
		Date fechaFin = vendedor.calcularFechaGaratia(fecha, producto.getPrecio());
		GarantiaExtendida garantiaExtendida = new GarantiaExtendida(producto, fechaInicial, fechaFin, precioGarantia,
				NOMBRE_CLIENTE);
		repositorioGarantia.agregar(garantiaExtendida);
		GarantiaExtendida garantia = repositorioGarantia.obtener(producto.getCodigo());
		// Se valida la garantia
		Assert.assertNotNull(garantia);
		// Se valida que el producto tenga garantía
		Assert.assertTrue(vendedor.tieneGarantia(producto.getCodigo()));
		// Se valida que el producto de la garantía coincida con el enviado para el
		// registro
		Assert.assertEquals(garantia.getProducto().getCodigo(), producto.getCodigo());
		// Se valida que el precio calculado sea igual al esperado
		Assert.assertTrue(garantia.getPrecioGarantia() == 156000.0);
		// Se valida la fecha de finalización
		Assert.assertTrue(formatter.format(garantia.getFechaFinGarantia()).equals("09/04/2019"));
		// Se valida el nombre del cliente
		Assert.assertTrue(garantia.getNombreCliente().equals(NOMBRE_CLIENTE));
	}

	/**
	 * Método que permite verificar que se cumpla la regla de negocio para los
	 * productos con precios menores o iguales a 50000. Se aplica 10% del precio
	 * para la garantía y 100 días a partir de la fecha actual.
	 * 
	 * El sistema registra la garantía para el producto, y se valida que el precio y
	 * la fecha de finalización coincidan con los esperados, y que el producto
	 * cuente con la nueva garantía
	 * 
	 * @throws ParseException En caso de error durante la creación de las fechas de
	 *                        prueba
	 */
	@Test
	public void garantiaExtendidaReglaDiezPorcientoCienDias() throws ParseException {
		Producto producto = new ProductoTestDataBuilder().conPrecio(450000.0).conNombre(COMPUTADOR_LENOVO).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		Date fechaInicial = formatter.parse("17/08/2018");
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaInicial);
		double precioGarantia = vendedor.calcularPrecioGarantia(producto.getPrecio(), 0.1);
		Date fechaFin = vendedor.calcularFechaGaratia(fecha, producto.getPrecio());
		System.out.println(fechaFin.toString());
		System.out.println(precioGarantia);
		GarantiaExtendida garantiaExtendida = new GarantiaExtendida(producto, fechaInicial, fechaFin, precioGarantia,
				NOMBRE_CLIENTE);
		repositorioGarantia.agregar(garantiaExtendida);
		GarantiaExtendida garantia = repositorioGarantia.obtener(producto.getCodigo());
		// Se valida la garantia
		Assert.assertNotNull(garantia);
		// Se valida que el producto tenga garantía
		Assert.assertTrue(vendedor.tieneGarantia(producto.getCodigo()));
		// Se valida que el producto de la garantía coincida con el enviado para el
		// registro
		Assert.assertEquals(garantia.getProducto().getCodigo(), producto.getCodigo());
		// Se valida que el precio calculado sea igual al esperado
		Assert.assertTrue(garantia.getPrecioGarantia() == 45000.0);
		// Se valida la fecha de finalización
		Assert.assertTrue(formatter.format(garantia.getFechaFinGarantia()).equals("25/11/2018"));
		// Se valida el nombre del cliente
		Assert.assertTrue(garantia.getNombreCliente().equals(NOMBRE_CLIENTE));
	}

}
