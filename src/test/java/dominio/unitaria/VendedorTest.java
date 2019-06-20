package dominio.unitaria;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import dominio.Producto;
import dominio.Vendedor;
import dominio.repositorio.RepositorioGarantiaExtendida;
import dominio.repositorio.RepositorioProducto;
import testdatabuilder.ProductoTestDataBuilder;

public class VendedorTest {

	@Test
	public void productoYaTieneGarantiaTest() {

		// arrange
		ProductoTestDataBuilder productoTestDataBuilder = new ProductoTestDataBuilder();

		Producto producto = productoTestDataBuilder.build();

		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		when(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo())).thenReturn(producto);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		boolean existeProducto = vendedor.tieneGarantia(producto.getCodigo());

		// assert
		assertTrue(existeProducto);
	}

	@Test
	public void productoNoTieneGarantiaTest() {

		// arrange
		ProductoTestDataBuilder productoestDataBuilder = new ProductoTestDataBuilder();

		Producto producto = productoestDataBuilder.build();

		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		when(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo())).thenReturn(null);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		boolean existeProducto = vendedor.tieneGarantia(producto.getCodigo());

		// assert
		assertFalse(existeProducto);
	}

	/**
	 * Método que permite verificar que se este realizando el calculo del precio de
	 * la garantía correctamente, para el caso en el que se aplica el 20% del valor
	 * del producto
	 */
	@Test
	public void precioGarantiaVeintePorcientoTest() {
		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		double precioGarantia = vendedor.calcularPrecioGarantia(650000, 0.2);
		assertTrue(precioGarantia == 130000.0);
	}

	/**
	 * Método que permite verificar que se este realizando el calculo del precio de
	 * la garantía correctamente, para el caso en el que se aplica el 10% del valor
	 * del producto
	 */
	@Test
	public void precioGarantiaDiezPorcientoTest() {
		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		double precioGarantia = vendedor.calcularPrecioGarantia(450000, 0.1);
		assertTrue(precioGarantia == 45000.0);
	}

	/**
	 * Método que permite verificar el calculo de la fecha de finalización de la
	 * garantía para la regla de negocio donde se aplican 200 días de garantía
	 * 
	 * @throws ParseException En caso de error durante la creación de las fechas de
	 *                        prueba
	 */
	@Test
	public void fechaFinalizacionDoscientosDias() throws Exception {
		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		ProductoTestDataBuilder productoestDataBuilder = new ProductoTestDataBuilder();
		Producto producto = productoestDataBuilder.build();

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date fechaIniciaGarantia = formatter.parse("16/08/2018");
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaIniciaGarantia);
		Date fechaFinGarantia = vendedor.calcularFechaGaratia(fecha, producto.getPrecio());

		assertTrue(formatter.format(fechaFinGarantia).equals("06/04/2019"));
	}
	
	/**
	 * Método que permite verificar el calculo de la fecha de finalización de la
	 * garantía para la regla de negocio donde se aplican 100 días de garantía
	 * 
	 * @throws ParseException En caso de error durante la creación de las fechas de
	 *                        prueba
	 */
	@Test
	public void fechaFinalizacionCienDias() throws Exception {
		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		ProductoTestDataBuilder productoestDataBuilder = new ProductoTestDataBuilder();
		Producto producto = productoestDataBuilder.conPrecio(450000.0).build();

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date fechaIniciaGarantia = formatter.parse("17/08/2018");
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaIniciaGarantia);
		Date fechaFinGarantia = vendedor.calcularFechaGaratia(fecha, producto.getPrecio());

		assertTrue(formatter.format(fechaFinGarantia).equals("25/11/2018"));
	}

	/**
	 * Método que permite verificar si la validación del número de vocales en un
	 * código se realiza correctamente, para el caso en que este no posee tal número
	 * de vocales
	 */
	@Test
	public void codigoNoTieneTresVocales() {
		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		boolean resultadoValidacion = vendedor.validarVocales("A213398i");

		assertFalse(resultadoValidacion);
	}

	/**
	 * Método que permite verificar si la validación del número de vocales en un
	 * código se realiza correctamente, para el caso en que este si posee tal número
	 * de vocales
	 */
	@Test
	public void codigoSiTieneTresVocales() {
		RepositorioGarantiaExtendida repositorioGarantia = mock(RepositorioGarantiaExtendida.class);
		RepositorioProducto repositorioProducto = mock(RepositorioProducto.class);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		boolean resultadoValidacion = vendedor.validarVocales("A213e98i");

		assertTrue(resultadoValidacion);
	}
}
