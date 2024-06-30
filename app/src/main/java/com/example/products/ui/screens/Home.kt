package com.example.products.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.products.ProductsTopAppBar
import com.example.products.R
import com.example.products.data.Product
import com.example.products.helpers.LocalStorage
import com.example.products.navigation.NavigationDestination
import com.example.products.ui.AppViewModelFactory
import kotlinx.coroutines.launch


object HomeDestination : NavigationDestination {
    override val source: String = "home"
    override val titleRes: Int = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    localStorage: LocalStorage = LocalStorage(LocalContext.current),
    navController: NavController = rememberNavController(),
    onAddProduct: () -> Unit = {},
    onClickSettings: () -> Unit = {},
    onClickProduct: (Int) -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val extendedFabState = remember {
        ExtendedFabState()
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0) extendedFabState.collapse()
                else if (available.y > 0) extendedFabState.expand()

                return Offset.Zero
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val homeUiState by homeViewModel.homeUiState.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    var dollarByLbp =
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("dollarByLbp") ?: ""
    if (dollarByLbp.isBlank()) dollarByLbp =
        localStorage.getString(SettingsKeys.DOLLAR_BY_LBP.keyName) ?: ""

    Scaffold(
        topBar = {
            ProductsTopAppBar(
                title = stringResource(id = HomeDestination.titleRes),
                canNavigateBack = false, showSettings = true, onClickSettings = onClickSettings
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = stringResource(id = R.string.add),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Normal
                    )
                },
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = "") },
                onClick = onAddProduct,
                expanded = extendedFabState.isExpanded
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) {
        HomeBody(
            onClickProduct = onClickProduct, onDelete = { product ->
                coroutineScope.launch {
                    homeViewModel.deleteProduct(product)
                }
            }, onSearch = { searchKeyword ->
                homeViewModel.updateSearchQuery(searchKeyword)
            },
            onClickDollarLbpComponent = { navController.navigate(SettingsDestination.source) },
            productsList = homeUiState.products,
            dollarByLbp = dollarByLbp,
            modifier = Modifier
                .padding(it)
                .padding(
                    start = dimensionResource(id = R.dimen.padding_small),
                    end = dimensionResource(id = R.dimen.padding_small),
                    top = dimensionResource(id = R.dimen.padding_small)
                )
                .fillMaxSize(), value = searchQuery
        )
    }
}

@Composable
fun HomeBody(
    onClickProduct: (Int) -> Unit,
    onDelete: (Product) -> Unit,
    onSearch: (String) -> Unit,
    productsList: List<Product>,
    dollarByLbp: String? = null,
    onClickDollarLbpComponent: () -> Unit,
    modifier: Modifier,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SearchInput(onSearch = onSearch, value = value, modifier = Modifier.weight(2f))

            if (dollarByLbp != null) {
                AnimatedVisibility(visible = dollarByLbp.isNotBlank()) {
                    DollarLbpComponent(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = dimensionResource(id = R.dimen.padding_small))
                            .clickable { onClickDollarLbpComponent() },
                        price = dollarByLbp,
                        priceFontSize = 18,
                        labelFontSize = 10
                    )
                }
            }
        }

        if (productsList.isEmpty()) {
            LottieEmptyList()
        } else {
            Text(
                text = stringResource(id = R.string.products_nb, productsList.size),
                fontWeight = FontWeight.Thin,
                fontSize = 12.sp,
                fontStyle = FontStyle.Normal,
                maxLines = 1
            )

            ProductsList(
                { onClickProduct(it.id) }, onDelete,
                productsList,
                dollarByLbp
            )
        }
    }
}

@Composable
fun ProductsList(
    onClickProduct: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    productsList: List<Product>,
    dollarByLbp: String?,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        items(items = productsList, key = { it.id }) {
            ProductCell(
                data = it,
                modifier = Modifier
                    .clickable { onClickProduct(it) }
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(bottom = dimensionResource(id = R.dimen.padding_small)),
                dollarByLbp = dollarByLbp,
                onDelete = onDelete
            )
        }
    }
}

@Composable
fun ProductCell(
    modifier: Modifier = Modifier,
    data: Product,
    dollarByLbp: String?,
    onDelete: (Product) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        horizontal = dimensionResource(
                            id = R.dimen.padding_small
                        )
                    ),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = data.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    fontStyle = FontStyle.Normal, maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (data.description.isNotBlank()) {
                    Text(
                        text = data.description, fontWeight = FontWeight.Thin,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        fontStyle = FontStyle.Normal,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = data.getFormattedPriceByDollar(dollarByLbp),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
                fontStyle = FontStyle.Normal
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { isMenuExpanded = !isMenuExpanded }
                ) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }) {

                    AddDropDownMenuItem(
                        text = stringResource(id = R.string.delete_dropdown),
                        textColor = Color.Red,
                        onclick = {
                            onDelete(data)
                            isMenuExpanded = false
                        })
                }
            }
        }
    }
}

@Composable
fun AddDropDownMenuItem(
    text: String,
    textColor: Color,
    textSize: TextUnit = 16.sp,
    onclick: () -> Unit = {}
) {
    DropdownMenuItem(text = {
        Text(text = text, color = textColor, fontSize = textSize, textAlign = TextAlign.Center)
    }, onClick = onclick)
}

@Composable
fun LottieEmptyList(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.empty_list))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        LottieAnimation(
            progress = progress,
            composition = composition,
            modifier = Modifier.size(150.dp)
        )

        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(id = R.string.no_available_items),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SearchInput(modifier: Modifier = Modifier, onSearch: (String) -> Unit, value: String) {
    var isFocused by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        label = "",
        animationSpec = tween(2000)
    )

    OutlinedTextField(
        value = value,
        onValueChange = {
            onSearch(it)
        },
        textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.SemiBold),
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_field_placeHolder),
                fontWeight = FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
        },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .animatedBorder(
                { progress },
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.onSurface
            ),
        singleLine = true
    )
}

fun Modifier.animatedBorder(
    provideProgress: () -> Float,
    colorFocused: Color,
    colorUnfocused: Color
) = this.drawWithCache {
    val width = size.width
    val height = size.height

    val shape = CircleShape

    // Only works with RoundedCornerShape...
    val outline = shape.createOutline(size, layoutDirection, this) as Outline.Rounded

    // ... correction: Only works with same corner sizes everywhere
    val radius = outline.roundRect.topLeftCornerRadius.x
    val diameter = 2 * radius

    // Clockwise path
    val pathCw = Path()

    // Start top center
    pathCw.moveTo(width / 2, 0f)

    // Line to right
    pathCw.lineTo(width - radius, 0f)

    // Top right corner
    pathCw.arcTo(Rect(width - diameter, 0f, width, diameter), -90f, 90f, false)

    // Right edge
    pathCw.lineTo(width, height - radius)

    // Bottom right corner
    pathCw.arcTo(Rect(width - diameter, height - diameter, width, height), 0f, 90f, false)

    // Line to bottom center
    pathCw.lineTo(width / 2, height)

    // As above, but mirrored horizontally
    val pathCcw = Path()
    pathCcw.moveTo(width / 2, 0f)
    pathCcw.lineTo(radius, 0f)
    pathCcw.arcTo(Rect(0f, 0f, diameter, diameter), -90f, -90f, false)
    pathCcw.lineTo(0f, height - radius)
    pathCcw.arcTo(Rect(0f, height - diameter, diameter, height), 180f, -90f, false)
    pathCcw.lineTo(width / 2, height)

    val pmCw = PathMeasure().apply {
        setPath(pathCw, false)
    }

    val pmCcw = PathMeasure().apply {
        setPath(pathCcw, false)
    }

    fun DrawScope.drawIndicator(progress: Float, pathMeasure: PathMeasure) {
        val subPath = Path()
        pathMeasure.getSegment(0f, pathMeasure.length * EaseOut.transform(progress), subPath)
        drawPath(subPath, colorFocused, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }

    onDrawBehind {
        // Draw the shape
        drawOutline(outline, colorUnfocused, style = Stroke(1.dp.toPx()))

        // Draw the indicators
        drawIndicator(provideProgress(), pmCw)
        drawIndicator(provideProgress(), pmCcw)
    }
}

@Stable
class ExtendedFabState(initialValue: Boolean = true) {
    var isExpanded by mutableStateOf(initialValue)
        private set

    fun expand() {
        isExpanded = true
    }

    fun collapse() {
        isExpanded = false
    }
}